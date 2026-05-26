package com.aicloud.framework.mq.core;

import com.aicloud.framework.mq.config.AicloudMqProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;

/**
 * Default local event publisher used when no MQ adapter is configured.
 *
 * @author yifei
 */
public class LocalMessagePublisher implements MessagePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(LocalMessagePublisher.class);

    private final ApplicationEventPublisher applicationEventPublisher;
    private final AicloudMqProperties properties;

    public LocalMessagePublisher(ApplicationEventPublisher applicationEventPublisher,
                                 AicloudMqProperties properties) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.properties = properties;
    }

    @Override
    public void publish(DomainEvent event) {
        if (!StringUtils.hasText(event.getTopic())) {
            event.setTopic(properties.getDefaultTopic());
        }
        applicationEventPublisher.publishEvent(event);
        LOG.info("domain_event_published eventId={} topic={} eventType={} tenantId={}",
                event.getEventId(), event.getTopic(), event.getEventType(), event.getTenantId());
    }
}
