package com.aicloud.framework.tenant.config;

import com.aicloud.framework.tenant.core.TenantContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Tenant auto configuration.
 *
 * @author yifei
 */
@AutoConfiguration
@EnableConfigurationProperties(TenantProperties.class)
@ConditionalOnProperty(prefix = "aicloud.tenant", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AicloudTenantAutoConfiguration {

    @Bean
    @ConditionalOnClass(FilterRegistrationBean.class)
    @ConditionalOnMissingBean(TenantContextFilter.class)
    public FilterRegistrationBean<TenantContextFilter> tenantContextFilter(TenantProperties tenantProperties) {
        FilterRegistrationBean<TenantContextFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new TenantContextFilter(tenantProperties));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        bean.addUrlPatterns("/*");
        return bean;
    }
}
