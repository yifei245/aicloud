package com.aicloud.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class GatewayOpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenApi() {
        Components components = new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Bearer <accessToken>")
                        .description("管理端鉴权头。先调用 /auth/login 获取 accessToken，再填写 Bearer Token。"))
                .addSecuritySchemes("tenantHeader", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Tenant-Id")
                        .description("租户头。默认演示环境固定为 1。"))
                .addSecuritySchemes("openapiAppKey", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-App-Key")
                        .description("第三方 OpenAPI 应用标识。演示值：demo_app_key"))
                .addSecuritySchemes("openapiTimestamp", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Timestamp")
                        .description("第三方 OpenAPI 请求时间戳，单位秒。"))
                .addSecuritySchemes("openapiNonce", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Nonce")
                        .description("第三方 OpenAPI 随机串，用于防重放。"))
                .addSecuritySchemes("openapiSign", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Sign")
                        .description("第三方 OpenAPI HMAC-SHA256 签名。"))
                .addParameters("AuthorizationHeader", new Parameter()
                        .in("header")
                        .name("Authorization")
                        .description("格式：Bearer <accessToken>")
                        .schema(new StringSchema().example("Bearer atk_xxxxxxxxxxxxxxxx")))
                .addParameters("TenantHeader", new Parameter()
                        .in("header")
                        .name("X-Tenant-Id")
                        .description("默认租户 ID")
                        .schema(new StringSchema().example("1")));

        String description = """
                <p>AICloud 网关管理接口与聚合文档入口。</p>
                <h3>全局鉴权说明</h3>
                <ol>
                  <li>管理端接口统一走网关，常用请求头：<code>Authorization: Bearer &lt;accessToken&gt;</code>、<code>X-Tenant-Id: 1</code>。</li>
                  <li>管理员测试账号：<code>admin / 123456</code>，登录终端：<code>ADMIN</code>。</li>
                  <li>第三方 OpenAPI 接口额外需要：<code>X-App-Key</code>、<code>X-Timestamp</code>、<code>X-Nonce</code>、<code>X-Sign</code>。</li>
                </ol>
                <h3>一键测试顺序</h3>
                <ol>
                  <li>进入 <code>auth</code> 分组，调用 <code>POST /auth/login</code> 获取 <code>accessToken</code>。</li>
                  <li>在后续管理端接口请求头中带上 <code>Authorization</code> 和 <code>X-Tenant-Id: 1</code>。</li>
                  <li>进入 <code>system</code> 分组，先测 <code>GET /system/menu/tree</code> 与 <code>GET /system/menu/buttons</code>。</li>
                  <li>进入 <code>product</code>、<code>trade</code>、<code>pay</code> 分组，验证业务接口连通性。</li>
                  <li>进入 <code>openapi</code> 分组，按签名规则测试 <code>GET /openapi/v1/ping</code>。</li>
                </ol>
                <h3>推荐默认值</h3>
                <ul>
                  <li><code>tenantId = 1</code></li>
                  <li><code>username = admin</code></li>
                  <li><code>password = 123456</code></li>
                  <li><code>terminal = ADMIN</code></li>
                  <li><code>X-App-Key = demo_app_key</code></li>
                </ul>
                """;

        return new OpenAPI()
                .info(new Info()
                        .title("AICloud Gateway API")
                        .version("v1")
                        .description(description))
                .addServersItem(new Server().url("http://127.0.0.1:48080").description("本地开发网关"))
                .components(components);
    }
}
