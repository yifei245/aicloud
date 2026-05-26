package com.aicloud.framework.mq.config;

import com.aicloud.framework.mq.core.LocalMessagePublisher;
import com.aicloud.framework.mq.core.MessagePublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

/**
 * MQ auto configuration.
 *
 * @author yifei
 */
@AutoConfiguration
@EnableConfigurationProperties(AicloudMqProperties.class)
@ConditionalOnProperty(prefix = "aicloud.mq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AicloudMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MessagePublisher messagePublisher(ApplicationEventPublisher applicationEventPublisher,
                                             AicloudMqProperties properties) {
        return new LocalMessagePublisher(applicationEventPublisher, properties);
    }
}
