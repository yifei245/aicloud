package com.aicloud.framework.mybatis.core.integration;

import cn.hutool.crypto.digest.DigestUtil;
import com.aicloud.framework.common.util.json.JsonUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Supplier;

/** Target deduplication and business writes share the same local transaction. */
public class IntegrationInboxService {
    private final JdbcTemplate jdbc;
    public IntegrationInboxService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(rollbackFor = Exception.class)
    public <T> T execute(Long tenantId, String consumer, String businessKey, Object request, Class<T> resultType, Supplier<T> action) {
        if (tenantId == null || tenantId <= 0 || businessKey == null || businessKey.length() > 200) {
            throw new IllegalArgumentException("Invalid integration tenant or business key");
        }
        String hash = DigestUtil.sha256Hex(JsonUtils.toJsonString(request));
        try {
            jdbc.update("INSERT INTO integration_inbox(tenant_id,consumer,business_key,payload_hash,status) VALUES(?,?,?,?,'PROCESSING')", tenantId, consumer, businessKey, hash);
        } catch (DuplicateKeyException ignored) { /* The row lock below waits for any previous writer. */ }
        Map<String, Object> record = jdbc.queryForMap("SELECT payload_hash,status,result_json FROM integration_inbox WHERE tenant_id=? AND consumer=? AND business_key=? FOR UPDATE",
                tenantId, consumer, businessKey);
        if (!hash.equals(record.get("payload_hash"))) throw new IllegalArgumentException("Integration business key already has different content");
        if ("DONE".equals(record.get("status"))) return JsonUtils.parseObject((String) record.get("result_json"), resultType);
        T result = action.get();
        jdbc.update("UPDATE integration_inbox SET status='DONE',result_json=?,update_time=CURRENT_TIMESTAMP WHERE tenant_id=? AND consumer=? AND business_key=?",
                JsonUtils.toJsonString(result), tenantId, consumer, businessKey);
        return result;
    }
}
