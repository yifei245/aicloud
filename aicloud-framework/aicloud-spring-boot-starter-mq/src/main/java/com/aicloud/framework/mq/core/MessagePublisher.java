package com.aicloud.framework.mq.core;

/**
 * Message publisher abstraction. Infrastructure adapters can replace the default local publisher.
 *
 * @author yifei
 */
public interface MessagePublisher {

    /**
     * Publish domain event.
     *
     * @param event domain event
     */
    void publish(DomainEvent event);
}
