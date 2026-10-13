package com.aicloud.module.oa.framework.web.config;

import com.aicloud.framework.swagger.config.AicloudSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * oa 模块的 web 组件的 Configuration
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class OaWebConfiguration {

    /**
     * oa 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi oaGroupedOpenApi() {
        return AicloudSwaggerAutoConfiguration.buildGroupedOpenApi("oa");
    }

}
