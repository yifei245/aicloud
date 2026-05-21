package com.aicloud.gateway.filter;

import com.aicloud.gateway.config.RoutePermissionProperties;
import com.aicloud.gateway.service.GatewayAuditLogService;
import com.aicloud.gateway.service.OpenApiClusterGuardService;
import com.aicloud.gateway.service.OpenApiAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger SECURITY_AUDIT_LOG = LoggerFactory.getLogger("GatewaySecurityAudit");
    private static final List<String> WHITELIST_PATHS = List.of(
            "/auth/login",
            "/auth/sso/token",
            "/auth/token/refresh",
            "/auth/token/verify",
            "/doc.html",
            "/webjars",
            "/swagger-resources",
            "/favicon.ico",
            "/swagger-ui",
            "/v3/api-docs"
    );

    private final RoutePermissionProperties routePermissionProperties;
    private final GatewayAuditLogService gatewayAuditLogService;
    private final OpenApiAuthService openApiAuthService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthGlobalFilter(RoutePermissionProperties routePermissionProperties,
                            GatewayAuditLogService gatewayAuditLogService,
                            OpenApiAuthService openApiAuthService,
                            @Value("${aicloud.gateway.auth-service-url:http://127.0.0.1:48081}") String authServiceUrl) {
        this.routePermissionProperties = routePermissionProperties;
        this.gatewayAuditLogService = gatewayAuditLogService;
        this.openApiAuthService = openApiAuthService;
        this.webClient = WebClient.builder().baseUrl(authServiceUrl).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod() == null ? "" : exchange.getRequest().getMethod().name();
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }
        if (isOpenApiPath(path)) {
            return Mono.fromCallable(() -> openApiAuthService.verify(exchange))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(verifyResult -> {
                        applyRateLimitHeaders(exchange, verifyResult.getRateLimitDecision());
                        if (!verifyResult.isSuccess()) {
                            String reason = withRateLimitReason(verifyResult.getReason(), verifyResult.getRateLimitDecision());
                            audit(exchange, "DENY", reason, null, null, null, null);
                            if ("OPENAPI_RATE_LIMIT_EXCEEDED".equals(verifyResult.getReason())) {
                                return tooManyRequests(exchange, "OpenAPI rate limit exceeded");
                            }
                            return unauthorized(exchange, "OpenAPI signature verification failed");
                        }
                        String tenantId = String.valueOf(verifyResult.getTenantId());
                        String username = StringUtils.hasText(verifyResult.getAppName())
                                ? verifyResult.getAppName()
                                : verifyResult.getAppKey();
                        MatchedRule matchedRule = findMatchedRule(method, path, "OPENAPI", "OPENAPI");
                        if (matchedRule != null && "DENY".equalsIgnoreCase(matchedRule.effect())) {
                            audit(exchange, "DENY",
                                    withRateLimitReason("RULE_DENY", verifyResult.getRateLimitDecision()),
                                    tenantId, null, username, matchedRule);
                            return forbidden(exchange, "Access denied by gateway rule");
                        }
                        if (matchedRule != null && StringUtils.hasText(matchedRule.permission())) {
                            audit(exchange, "DENY",
                                    withRateLimitReason("PERMISSION_MISSING", verifyResult.getRateLimitDecision()),
                                    tenantId, null, username, matchedRule);
                            return forbidden(exchange, "Permission denied: " + matchedRule.permission());
                        }
                        if (matchedRule != null) {
                            audit(exchange, "ALLOW",
                                    withRateLimitReason("RULE_ALLOW", verifyResult.getRateLimitDecision()),
                                    tenantId, null, username, matchedRule);
                        }
                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(builder -> builder
                                        .header("X-Tenant-Id", tenantId)
                                        .header("X-User-Id", "")
                                        .header("X-Username", username)
                                        .header("X-User-Type", "OPENAPI")
                                        .header("X-Login-Terminal", "OPENAPI")
                                        .header("X-App-Key", verifyResult.getAppKey())
                                        .header("X-User-Roles", "[]")
                                        .header("X-User-Permissions", "[]"))
                                .build();
                        return chain.filter(mutatedExchange);
                    })
                    .onErrorResume(ex -> {
                        audit(exchange, "DENY", "OPENAPI_AUTH_ERROR", null, null, null, null);
                        return unauthorized(exchange, "OpenAPI auth unavailable");
                    });
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            audit(exchange, "DENY", "MISSING_AUTH_HEADER", null, null, null, null);
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/auth/token/verify").queryParam("token", token).build())
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(body -> {
                    try {
                        JsonNode root = objectMapper.readTree(body);
                        int code = root.path("code").asInt(-1);
                        JsonNode data = root.path("data");
                        boolean valid = data.path("valid").asBoolean(false);
                        if (code != 0 || !valid) {
                            audit(exchange, "DENY", "TOKEN_VERIFY_FAILED", null, null, null, null);
                            return unauthorized(exchange, "Token verification failed");
                        }
                        String tokenTenantId = data.path("tenantId").asText("");
                        String userId = data.path("userId").asText("");
                        String username = data.path("username").asText("");
                        String requestTenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
                        if (requestTenantId != null && !requestTenantId.isBlank() && !requestTenantId.equals(tokenTenantId)) {
                            audit(exchange, "DENY", "TENANT_MISMATCH", tokenTenantId, userId, username, null);
                            return forbidden(exchange, "Cross-tenant request is forbidden");
                        }
                        String loginTerminal = data.path("loginTerminal").asText("");
                        String userType = data.path("userType").asText("");
                        MatchedRule matchedRule = findMatchedRule(method, path, loginTerminal, userType);
                        if (matchedRule != null && "DENY".equalsIgnoreCase(matchedRule.effect())) {
                            audit(exchange, "DENY", "RULE_DENY", tokenTenantId, userId, username, matchedRule);
                            return forbidden(exchange, "Access denied by gateway rule");
                        }
                        if (matchedRule != null
                                && StringUtils.hasText(matchedRule.permission())
                                && !hasPermission(data.path("permissions"), matchedRule.permission())) {
                            audit(exchange, "DENY", "PERMISSION_MISSING", tokenTenantId, userId, username, matchedRule);
                            return forbidden(exchange, "Permission denied: " + matchedRule.permission());
                        }
                        if (matchedRule != null) {
                            audit(exchange, "ALLOW", "RULE_ALLOW", tokenTenantId, userId, username, matchedRule);
                        }

                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(builder -> builder
                                        .header("X-Tenant-Id", tokenTenantId)
                                        .header("X-User-Id", data.path("userId").asText(""))
                                        .header("X-Username", data.path("username").asText(""))
                                        .header("X-User-Type", data.path("userType").asText(""))
                                        .header("X-Login-Terminal", loginTerminal)
                                        .header("X-User-Roles", data.path("roles").toString())
                                        .header("X-User-Permissions", data.path("permissions").toString()))
                                .build();
                        return chain.filter(mutatedExchange);
                    } catch (Exception ex) {
                        audit(exchange, "DENY", "VERIFY_RESPONSE_PARSE_ERROR", null, null, null, null);
                        return unauthorized(exchange, "Failed to parse auth response");
                    }
                })
                .onErrorResume(ex -> {
                    audit(exchange, "DENY", "AUTH_SERVICE_UNAVAILABLE", null, null, null, null);
                    return unauthorized(exchange, "Auth service unavailable");
                });
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isOpenApiPath(String path) {
        return path != null && path.startsWith("/openapi/v1/");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return writeJson(exchange.getResponse(), HttpStatus.UNAUTHORIZED, message);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return writeJson(exchange.getResponse(), HttpStatus.FORBIDDEN, message);
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange, String message) {
        return writeJson(exchange.getResponse(), HttpStatus.TOO_MANY_REQUESTS, message);
    }

    private void applyRateLimitHeaders(ServerWebExchange exchange,
                                       OpenApiClusterGuardService.RateLimitDecision decision) {
        if (decision == null) {
            return;
        }
        exchange.getResponse().getHeaders().set("X-RateLimit-Limit", String.valueOf(decision.limit()));
        exchange.getResponse().getHeaders().set("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
        exchange.getResponse().getHeaders().set("X-RateLimit-Reset", String.valueOf(decision.resetAt()));
    }

    private String withRateLimitReason(String reason, OpenApiClusterGuardService.RateLimitDecision decision) {
        if (decision == null) {
            return reason;
        }
        return reason + "|limit=" + decision.limit()
                + "|remaining=" + decision.remaining()
                + "|resetAt=" + decision.resetAt();
    }

    private Mono<Void> writeJson(ServerHttpResponse response, HttpStatus status, String message) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":" + status.value() + ",\"msg\":\"" + message + "\",\"data\":null}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    private MatchedRule findMatchedRule(String method, String path, String loginTerminal, String userType) {
        RoutePermissionProperties.Rule selected = null;
        for (RoutePermissionProperties.Rule rule : routePermissionProperties.getPermissionRules()) {
            if (!isRuleEnabled(rule) || !StringUtils.hasText(rule.getPath())) {
                continue;
            }
            if (isRuleMatched(rule, method, path, loginTerminal, userType)) {
                if (selected == null || safePriority(rule) > safePriority(selected)) {
                    selected = rule;
                }
            }
        }
        if (selected == null) {
            return null;
        }
        return new MatchedRule(
                selected.getName(),
                selected.getPermission(),
                normalizeEffect(selected.getEffect()),
                safePriority(selected));
    }

    private boolean hasPermission(JsonNode permissionsNode, String permission) {
        if (permissionsNode == null || !permissionsNode.isArray()) {
            return false;
        }
        for (JsonNode node : permissionsNode) {
            if (permission.equals(node.asText())) {
                return true;
            }
        }
        return false;
    }

    private boolean isRuleEnabled(RoutePermissionProperties.Rule rule) {
        return rule.getEnabled() == null || rule.getEnabled();
    }

    private int safePriority(RoutePermissionProperties.Rule rule) {
        return rule.getPriority() == null ? 0 : rule.getPriority();
    }

    private String normalizeEffect(String effect) {
        return StringUtils.hasText(effect) ? effect.toUpperCase() : "ALLOW";
    }

    private boolean isRuleMatched(RoutePermissionProperties.Rule rule, String method, String path, String loginTerminal, String userType) {
        boolean methodMatched = !StringUtils.hasText(rule.getMethod())
                || rule.getMethod().equalsIgnoreCase(method);
        boolean pathMatched = pathMatcher.match(rule.getPath(), path);
        boolean terminalMatched = rule.getTerminals() == null
                || rule.getTerminals().isEmpty()
                || rule.getTerminals().stream().anyMatch(item -> item.equalsIgnoreCase(loginTerminal));
        boolean userTypeMatched = rule.getUserTypes() == null
                || rule.getUserTypes().isEmpty()
                || rule.getUserTypes().stream().anyMatch(item -> item.equalsIgnoreCase(userType));
        return methodMatched && pathMatched && terminalMatched && userTypeMatched;
    }

    private void audit(ServerWebExchange exchange, String result, String reason,
                       String tenantId, String userId, String username, MatchedRule matchedRule) {
        String method = exchange.getRequest().getMethod() == null ? "" : exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String requestIp = resolveRequestIp(exchange);
        SECURITY_AUDIT_LOG.info(
                "gateway_audit result={} reason={} method={} path={} tenantId={} userId={} ruleName={} effect={} permission={} priority={}",
                result,
                reason,
                method,
                path,
                tenantId == null ? "" : tenantId,
                userId == null ? "" : userId,
                matchedRule == null ? "" : matchedRule.name(),
                matchedRule == null ? "" : matchedRule.effect(),
                matchedRule == null ? "" : matchedRule.permission(),
                matchedRule == null ? "" : matchedRule.priority());
        gatewayAuditLogService.logAsync(
                result,
                reason,
                method,
                path,
                tenantId,
                userId,
                username,
                requestIp,
                matchedRule == null ? null : matchedRule.effect(),
                matchedRule == null ? null : matchedRule.permission(),
                matchedRule == null ? null : matchedRule.priority()
        );
    }

    private String resolveRequestIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int idx = xff.indexOf(',');
            return idx > 0 ? xff.substring(0, idx).trim() : xff.trim();
        }
        if (exchange.getRequest().getRemoteAddress() != null
                && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "";
    }

    private record MatchedRule(String name, String permission, String effect, Integer priority) {
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
