package com.aicloud.module.bpm.framework.flowable.core.event;

import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.mybatis.core.integration.*;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.framework.tenant.core.util.TenantUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.annotation.Validated;

/** Publish business results only after the Flowable transaction commits, with durable retries. */
@AllArgsConstructor
@Validated
public class BpmProcessInstanceEventPublisher implements IntegrationEventHandler {
    private final ApplicationEventPublisher publisher;
    private final IntegrationOutboxService outbox;
    public void sendProcessInstanceResultEvent(@Valid BpmProcessInstanceStatusEvent event) {
        Long tenant = event.getTenantId() == null ? TenantContextHolder.getRequiredTenantId() : Long.valueOf(event.getTenantId());
        Result result = new Result();
        result.id=event.getId(); result.processDefinitionKey=event.getProcessDefinitionKey(); result.status=event.getStatus();
        result.reason=event.getReason(); result.businessKey=event.getBusinessKey(); result.tenantId=String.valueOf(tenant);
        outbox.enqueueOrdered(tenant,eventType(),"bpm-result:"+event.getId()+":"+event.getStatus(),"bpm-result:"+event.getId(),result);
    }
    @Override public String eventType() { return "BPM_BUSINESS_RESULT"; }
    @Override public void handle(IntegrationEvent delivery) {
        Result result=JsonUtils.parseObject(delivery.getPayload(),Result.class);
        TenantUtils.execute(delivery.getTenantId(),()-> {
            BpmProcessInstanceStatusEvent event=new BpmProcessInstanceStatusEvent(this);
            event.setId(result.id); event.setProcessDefinitionKey(result.processDefinitionKey); event.setStatus(result.status);
            event.setReason(result.reason); event.setBusinessKey(result.businessKey); event.setTenantId(result.tenantId);
            publisher.publishEvent(event);
        });
    }
    @lombok.Data public static class Result {
        private String id,processDefinitionKey,reason,businessKey,tenantId;
        private Integer status;
    }
}
