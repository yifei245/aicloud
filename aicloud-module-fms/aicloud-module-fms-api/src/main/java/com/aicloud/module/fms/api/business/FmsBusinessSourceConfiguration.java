package com.aicloud.module.fms.api.business;
import com.aicloud.framework.mybatis.core.integration.IntegrationOutboxService;
import org.springframework.context.annotation.*;
@Configuration(proxyBeanMethods = false)
@org.springframework.boot.autoconfigure.condition.ConditionalOnExpression("'${spring.application.name}' == 'erp-server' || '${spring.application.name}' == 'trade-server' || '${spring.application.name}' == 'oa-server' || '${spring.application.name}' == 'hrm-server'")
public class FmsBusinessSourceConfiguration {
    @Bean FmsBusinessBridge fmsBusinessBridge(IntegrationOutboxService outbox, FmsBusinessApi api) { return new FmsBusinessBridge(outbox, api); }
}
