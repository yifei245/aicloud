package com.aicloud.module.fms.api.business;

import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.mybatis.core.integration.*;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.framework.tenant.core.util.TenantUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

public class FmsBusinessBridge implements IntegrationEventHandler {
    private final IntegrationOutboxService outbox;
    private final FmsBusinessApi api;
    public FmsBusinessBridge(IntegrationOutboxService outbox, FmsBusinessApi api) { this.outbox = outbox; this.api = api; }
    public void record(String source, Long id, String no, BigDecimal amount, boolean create, String summary) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("财务来源必须与业务操作处于同一事务");
        if (amount == null || amount.signum() == 0) return;
        var event = new FmsBusinessApi.Event().setSource(source).setDocumentId(id).setDocumentNo(no)
                .setAmount(amount.abs().setScale(2, RoundingMode.UNNECESSARY)).setReverse(amount.signum() < 0).setAction(create ? "CREATE" : "CANCEL")
                .setOccurredAt(LocalDateTime.now()).setSummary(summary).setBusinessKey(source + ":" + id + ":" + UUID.randomUUID());
        outbox.enqueueOrdered(TenantContextHolder.getRequiredTenantId(), eventType(), event.getBusinessKey(), "fms:" + source + ":" + id, event);
    }
    @Override public String eventType() { return "FMS_BUSINESS_SOURCE"; }
    @Override public void handle(IntegrationEvent event) {
        TenantUtils.execute(event.getTenantId(), () -> api.deliver(JsonUtils.parseObject(event.getPayload(), FmsBusinessApi.Event.class)).checkError());
    }
}
