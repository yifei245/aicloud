package com.aicloud.framework.openapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * OpenAPI auto configuration.
 *
 * @author yifei
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(AicloudOpenApiProperties.class)
@ConditionalOnProperty(prefix = "aicloud.openapi", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AicloudOpenApiAutoConfiguration {

    private static final String AUTHORIZATION = "Authorization";
    private static final String TENANT_ID = "X-Tenant-Id";
    private static final String BEARER_AUTH = "bearerAuth";
    private static final String TENANT_HEADER = "tenantHeader";
    private static final String HTTP = "http";
    private static final String BEARER = "bearer";
    private static final String JWT = "JWT";
    private static final String API_KEY = "apiKey";
    private static final String HEADER = "header";

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI aicloudOpenApi(AicloudOpenApiProperties properties) {
        return new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion()))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(AUTHORIZATION)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(BEARER)
                                .bearerFormat(JWT))
                        .addSecuritySchemes(TENANT_HEADER, new SecurityScheme()
                                .name(TENANT_ID)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .scheme(API_KEY)))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH).addList(TENANT_HEADER));
    }
}
