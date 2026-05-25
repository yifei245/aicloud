package com.aicloud.gateway.service;

import com.aicloud.gateway.entity.AiAuditLog;
import com.aicloud.gateway.mapper.AuditLogMapper;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class GatewayAuditLogService {

    private final AuditLogMapper auditLogMapper;

    public GatewayAuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Async("auditTaskExecutor")
    public void logAsync(String result, String reason, String method, String path,
                         String tenantId, String userId, String username,
                         String requestIp, String effect, String permission, Integer priority) {
        try {
            AiAuditLog log = new AiAuditLog();
            log.setTenantId(parseLongOrDefault(tenantId, 1L));
            log.setUserId(parseLong(userId));
            log.setUsername(username);
            log.setModule("gateway");
            log.setOperation(buildOperation(reason, effect, permission, priority));
            log.setRequestUri(path);
            log.setRequestMethod(method);
            log.setRequestIp(requestIp);
            log.setSuccess("ALLOW".equalsIgnoreCase(result) ? 1 : 0);
            log.setErrorMsg("ALLOW".equalsIgnoreCase(result) ? null : reason);
            log.setCreateTime(LocalDateTime.now());
            auditLogMapper.insert(log);
        } catch (Exception ignored) {
            // Do not affect gateway request path.
        }
    }

    private String buildOperation(String reason, String effect, String permission, Integer priority) {
        StringBuilder builder = new StringBuilder("authz:").append(reason == null ? "UNKNOWN" : reason);
        if (effect != null && !effect.isBlank()) {
            builder.append("|effect=").append(effect);
        }
        if (permission != null && !permission.isBlank()) {
            builder.append("|perm=").append(permission);
        }
        if (priority != null) {
            builder.append("|priority=").append(priority);
        }
        return builder.toString();
    }

    private Long parseLong(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long parseLongOrDefault(String val, Long defaultValue) {
        Long parsed = parseLong(val);
        return parsed == null ? defaultValue : parsed;
    }
}
