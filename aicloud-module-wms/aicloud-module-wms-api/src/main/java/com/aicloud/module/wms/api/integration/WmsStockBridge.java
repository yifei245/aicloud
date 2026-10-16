package com.aicloud.module.wms.api.integration;

import cn.hutool.crypto.digest.DigestUtil;
import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.mybatis.core.integration.*;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.framework.tenant.core.util.TenantUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.*;

import java.util.Map;

/** Records a durable source intent before contacting WMS, then locks it in the business transaction. */
public class WmsStockBridge implements IntegrationEventHandler {
    private final JdbcTemplate jdbc;
    private final WmsStockApi api;
    private final IntegrationOutboxService outbox;
    private final TransactionTemplate independent;
    private final TransactionTemplate transaction;
    private final String source;

    public WmsStockBridge(JdbcTemplate jdbc, WmsStockApi api, IntegrationOutboxService outbox,
                          PlatformTransactionManager manager, String source) {
        this.jdbc = jdbc; this.api = api; this.outbox = outbox; this.source = source;
        independent = new TransactionTemplate(manager);
        independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction = new TransactionTemplate(manager);
    }

    public WmsStockApi.Balance prepare(WmsStockApi.Movement request, boolean commitAfterSource) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("库存预占必须处于源业务事务中");
        }
        request.setSource(source);
        Long tenant = TenantContextHolder.getRequiredTenantId();
        String hash = DigestUtil.sha256Hex(JsonUtils.toJsonString(request));
        independent.executeWithoutResult(status -> {
            try {
                jdbc.update("INSERT INTO integration_stock_intent(tenant_id,source,business_key,payload_hash,state) VALUES(?,?,?,?,'WAITING')",
                        tenant, source, request.getBusinessKey(), hash);
            } catch (DuplicateKeyException ignored) { }
        });
        Map<String, Object> intent = jdbc.queryForMap("SELECT payload_hash,state FROM integration_stock_intent WHERE tenant_id=? AND source=? AND business_key=? FOR UPDATE",
                tenant, source, request.getBusinessKey());
        if (!hash.equals(intent.get("payload_hash")) || "ABORT".equals(intent.get("state"))) {
            throw new IllegalArgumentException("库存业务键已撤销或对应不同明细");
        }
        WmsStockApi.Command command = new WmsStockApi.Command().setSource(source).setBusinessKey(request.getBusinessKey());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    outbox.enqueueIndependent(tenant, eventType(), "stock-abort:" + source + ":" + request.getBusinessKey(),
                            new Delivery().setAction("ABORT").setCommand(command));
                }
            }
        });
        WmsStockApi.Balance balance = api.prepare(request).getCheckedData();
        setState(tenant, request.getBusinessKey(), commitAfterSource ? "COMMIT" : "RESERVED");
        if (commitAfterSource) enqueue(tenant, "COMMIT", command);
        return balance;
    }

    public void finish(String businessKey, boolean commit) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("库存结束必须处于业务事务中");
        Long tenant = TenantContextHolder.getRequiredTenantId();
        String state = jdbc.queryForObject("SELECT state FROM integration_stock_intent WHERE tenant_id=? AND source=? AND business_key=? FOR UPDATE",
                String.class, tenant, source, businessKey);
        String next = commit ? "COMMIT" : "ABORT";
        if (next.equals(state)) return;
        if (!"RESERVED".equals(state)) throw new IllegalArgumentException("库存预占状态不允许此操作：" + state);
        setState(tenant, businessKey, next);
        enqueue(tenant, next, new WmsStockApi.Command().setSource(source).setBusinessKey(businessKey));
    }

    /** FOR UPDATE waits for the source business transaction: WAITING after acquiring the lock means it rolled back. */
    public String decision(String businessKey) {
        return transaction.execute(status -> {
            Long tenant = TenantContextHolder.getRequiredTenantId();
            var rows = jdbc.queryForList("SELECT state FROM integration_stock_intent WHERE tenant_id=? AND source=? AND business_key=? FOR UPDATE",
                    tenant, source, businessKey);
            if (rows.isEmpty()) return "ABORT";
            String state = (String) rows.getFirst().get("state");
            if ("WAITING".equals(state)) {
                setState(tenant, businessKey, "ABORT");
                return "ABORT";
            }
            return state;
        });
    }

    private void setState(Long tenant, String key, String state) {
        jdbc.update("UPDATE integration_stock_intent SET state=?,update_time=CURRENT_TIMESTAMP WHERE tenant_id=? AND source=? AND business_key=?",
                state, tenant, source, key);
    }
    private void enqueue(Long tenant, String action, WmsStockApi.Command command) {
        outbox.enqueue(tenant, eventType(), "stock-" + action.toLowerCase() + ":" + source + ":" + command.getBusinessKey(),
                new Delivery().setAction(action).setCommand(command));
    }
    @Override public String eventType() { return "WMS_STOCK_DECISION"; }
    @Override public void handle(IntegrationEvent event) {
        Delivery delivery = JsonUtils.parseObject(event.getPayload(), Delivery.class);
        TenantUtils.execute(event.getTenantId(), () -> {
            if ("COMMIT".equals(delivery.getAction())) api.commit(delivery.getCommand()).checkError();
            else if ("ABORT".equals(delivery.getAction())) api.abort(delivery.getCommand()).checkError();
            else throw new IllegalArgumentException("未知库存动作");
        });
    }
    @lombok.Data public static class Delivery { private String action; private WmsStockApi.Command command; }
}
