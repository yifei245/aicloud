package com.aicloud.framework.mybatis.core.integration;

import lombok.Data;

/** Durable business event; the producer owns its payload and handler. */
@Data
public class IntegrationEvent {
    private Long id;
    private Long tenantId;
    private String producer;
    private String eventType;
    private String businessKey;
    private String payload;
    private Integer attempts;
}
