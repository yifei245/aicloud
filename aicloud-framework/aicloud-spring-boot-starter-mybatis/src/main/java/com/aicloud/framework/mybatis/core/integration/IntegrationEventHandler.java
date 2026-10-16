package com.aicloud.framework.mybatis.core.integration;

/** A producer supplies a fixed, typed destination; event data cannot choose a URL. */
public interface IntegrationEventHandler {
    String eventType();
    void handle(IntegrationEvent event);
}
