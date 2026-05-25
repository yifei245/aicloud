package com.aicloud.framework.web.audit;

/**
 * Publishes audit log event.
 *
 * @author yifei
 */
public interface AuditLogPublisher {

    /**
     * Publishes one audit event.
     *
     * @param event audit event
     */
    void publish(AuditLogEvent event);
}
