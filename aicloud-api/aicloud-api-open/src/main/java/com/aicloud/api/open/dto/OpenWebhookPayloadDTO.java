package com.aicloud.api.open.dto;

import java.util.Map;

/**
 * OpenAPI webhook payload DTO.
 *
 * @author yifei
 */
public class OpenWebhookPayloadDTO extends OpenApiBaseRequest {

    private String eventType;
    private String eventId;
    private Map<String, Object> payload;

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    
    public String toString() {
        return "OpenWebhookPayloadDTO{}";
    }
}
