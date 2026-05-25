package com.aicloud.framework.web.config;

import com.aicloud.framework.web.audit.AuditLogFilter;
import com.aicloud.framework.web.audit.AuditLogPublisher;
import com.aicloud.framework.web.audit.DefaultAuditLogPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Shared web auto configuration.
 *
 * @author yifei
 */
@AutoConfiguration
public class AicloudWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuditLogPublisher auditLogPublisher() {
        return new DefaultAuditLogPublisher();
    }

    @Bean
    @ConditionalOnClass(FilterRegistrationBean.class)
    @ConditionalOnProperty(prefix = "aicloud.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<AuditLogFilter> auditLogFilter(AuditLogPublisher auditLogPublisher) {
        FilterRegistrationBean<AuditLogFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new AuditLogFilter(auditLogPublisher));
        bean.setOrder(Ordered.LOWEST_PRECEDENCE - 20);
        bean.addUrlPatterns("/*");
        return bean;
    }
}
