package com.aicloud.framework.mq.core;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Standard domain event envelope.
 *
 * @author yifei
 */
public class DomainEvent {

    private String eventId = UUID.randomUUID().toString();
    private String topic;
    private String eventType;
    private Long tenantId;
    private Object payload;
    private Map<String, String> headers = Map.of();
    private LocalDateTime occurredAt = LocalDateTime.now();

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers == null ? Map.of() : headers;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
