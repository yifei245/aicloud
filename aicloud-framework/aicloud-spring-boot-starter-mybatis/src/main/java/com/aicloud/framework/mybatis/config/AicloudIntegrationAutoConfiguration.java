package com.aicloud.framework.mybatis.config;

import com.aicloud.framework.mybatis.core.integration.IntegrationEventHandler;
import com.aicloud.framework.mybatis.core.integration.IntegrationInboxService;
import com.aicloud.framework.mybatis.core.integration.IntegrationOutboxService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration(after = JdbcTemplateAutoConfiguration.class)
@ConditionalOnBean(JdbcTemplate.class)
@EnableScheduling
public class AicloudIntegrationAutoConfiguration {
    @Bean
    public IntegrationOutboxService integrationOutboxService(JdbcTemplate jdbc, @Value("${spring.application.name:aicloud}") String producer,
                                                            ObjectProvider<IntegrationEventHandler> handlers) {
        return new IntegrationOutboxService(jdbc, producer, () -> handlers.orderedStream().toList());
    }
    @Bean
    public IntegrationInboxService integrationInboxService(JdbcTemplate jdbc) { return new IntegrationInboxService(jdbc); }
}
