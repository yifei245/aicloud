package com.aicloud.framework.rbac.config;

import com.aicloud.framework.rbac.core.PermissionInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * RBAC auto configuration.
 *
 * @author yifei
 */
@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer.class)
@ConditionalOnProperty(prefix = "aicloud.rbac", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AicloudRbacAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PermissionInterceptor permissionInterceptor() {
        return new PermissionInterceptor();
    }

    @Bean
    public WebMvcConfigurer permissionWebMvcConfigurer(PermissionInterceptor permissionInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(permissionInterceptor).addPathPatterns("/**");
            }
        };
    }
}
