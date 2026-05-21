package com.aicloud.gateway.service;

import com.aicloud.gateway.config.OpenApiSecurityProperties;
import com.aicloud.gateway.entity.AiApiApp;
import com.aicloud.gateway.mapper.ApiAppMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

@Service
public class OpenApiAuthService {

    private final ApiAppMapper apiAppMapper;
    private final OpenApiSecurityProperties properties;
    private final OpenApiClusterGuardService clusterGuardService;

    public OpenApiAuthService(ApiAppMapper apiAppMapper,
                              OpenApiSecurityProperties properties,
                              OpenApiClusterGuardService clusterGuardService) {
        this.apiAppMapper = apiAppMapper;
        this.properties = properties;
        this.clusterGuardService = clusterGuardService;
    }

    public VerifyResult verify(ServerWebExchange exchange) {
        if (!properties.isEnabled()) {
            return VerifyResult.fail("OPENAPI_DISABLED");
        }
        String appKey = header(exchange, "X-App-Key");
        String timestamp = header(exchange, "X-Timestamp");
        String nonce = header(exchange, "X-Nonce");
        String sign = header(exchange, "X-Sign");
        if (!StringUtils.hasText(appKey) || !StringUtils.hasText(timestamp)
                || !StringUtils.hasText(nonce) || !StringUtils.hasText(sign)) {
            return VerifyResult.fail("OPENAPI_HEADER_MISSING");
        }

        Long timestampSeconds;
        try {
            timestampSeconds = Long.parseLong(timestamp);
        } catch (NumberFormatException ex) {
            return VerifyResult.fail("OPENAPI_TIMESTAMP_INVALID");
        }
        long nowSeconds = Instant.now().getEpochSecond();
        long maxDrift = Math.max(properties.getMaxTimeDriftSeconds(), 1);
        long maxFuture = Math.max(properties.getMaxFutureSeconds(), 0);
        if (timestampSeconds < (nowSeconds - maxDrift) || timestampSeconds > (nowSeconds + maxFuture)) {
            return VerifyResult.fail("OPENAPI_TIMESTAMP_EXPIRED");
        }

        AiApiApp app = apiAppMapper.selectOne(new LambdaQueryWrapper<AiApiApp>()
                .eq(AiApiApp::getAppKey, appKey)
                .eq(AiApiApp::getStatus, 1)
                .last("LIMIT 1"));
        if (app == null || !StringUtils.hasText(app.getAppSecret())) {
            return VerifyResult.fail("OPENAPI_APP_NOT_FOUND");
        }
        if (!isIpAllowed(exchange, app.getIpWhitelist())) {
            return VerifyResult.fail("OPENAPI_IP_NOT_ALLOWED");
        }
        OpenApiClusterGuardService.RateLimitDecision rateLimitDecision = null;
        if (properties.isRateLimitEnabled()) {
            rateLimitDecision = clusterGuardService.checkRateLimit(appKey);
            if (!rateLimitDecision.allowed()) {
                return VerifyResult.fail("OPENAPI_RATE_LIMIT_EXCEEDED", rateLimitDecision);
            }
        }
        if (properties.isReplayProtectEnabled()
                && clusterGuardService.isReplayRequest(appKey, timestamp + ":" + nonce, nowSeconds)) {
            return VerifyResult.fail("OPENAPI_NONCE_REPLAY");
        }

        String method = exchange.getRequest().getMethod() == null ? "" : exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String query = canonicalQuery(exchange.getRequest().getQueryParams().toSingleValueMap());
        String toSign = method + "\n" + path + "\n" + query + "\n" + timestamp + "\n" + nonce;
        String expected = hmacSha256Hex(toSign, app.getAppSecret());
        if (expected == null || !expected.equalsIgnoreCase(sign)) {
            return VerifyResult.fail("OPENAPI_SIGN_INVALID");
        }
        return VerifyResult.success(app.getTenantId(), app.getAppKey(), app.getAppName(), rateLimitDecision);
    }

    private String header(ServerWebExchange exchange, String name) {
        String val = exchange.getRequest().getHeaders().getFirst(name);
        return val == null ? "" : val.trim();
    }

    private boolean isIpAllowed(ServerWebExchange exchange, String ipWhitelist) {
        if (!StringUtils.hasText(ipWhitelist)) {
            return true;
        }
        String requestIp = resolveRequestIp(exchange);
        if (!StringUtils.hasText(requestIp)) {
            return false;
        }
        String[] ips = ipWhitelist.split(",");
        for (String ip : ips) {
            if (requestIp.equals(ip.trim())) {
                return true;
            }
        }
        return false;
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

    private String canonicalQuery(Map<String, String> queryMap) {
        if (queryMap == null || queryMap.isEmpty()) {
            return "";
        }
        List<String> keys = new ArrayList<>(queryMap.keySet());
        Collections.sort(keys);
        List<String> pairs = new ArrayList<>();
        for (String key : keys) {
            if (!StringUtils.hasText(key)) {
                continue;
            }
            if ("sign".equalsIgnoreCase(key) || "x-sign".equalsIgnoreCase(key)) {
                continue;
            }
            String value = queryMap.get(key);
            pairs.add(key + "=" + (value == null ? "" : value));
        }
        return String.join("&", pairs);
    }

    private String hmacSha256Hex(String text, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format(Locale.ROOT, "%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    public static final class VerifyResult {
        private final boolean success;
        private final String reason;
        private final Long tenantId;
        private final String appKey;
        private final String appName;
        private final OpenApiClusterGuardService.RateLimitDecision rateLimitDecision;

        private VerifyResult(boolean success, String reason, Long tenantId, String appKey, String appName,
                             OpenApiClusterGuardService.RateLimitDecision rateLimitDecision) {
            this.success = success;
            this.reason = reason;
            this.tenantId = tenantId;
            this.appKey = appKey;
            this.appName = appName;
            this.rateLimitDecision = rateLimitDecision;
        }

        public static VerifyResult success(Long tenantId, String appKey, String appName,
                                           OpenApiClusterGuardService.RateLimitDecision rateLimitDecision) {
            return new VerifyResult(true, null, tenantId, appKey, appName, rateLimitDecision);
        }

        public static VerifyResult fail(String reason) {
            return new VerifyResult(false, reason, null, null, null, null);
        }

        public static VerifyResult fail(String reason, OpenApiClusterGuardService.RateLimitDecision rateLimitDecision) {
            return new VerifyResult(false, reason, null, null, null, rateLimitDecision);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getReason() {
            return reason;
        }

        public Long getTenantId() {
            return tenantId;
        }

        public String getAppKey() {
            return appKey;
        }

        public String getAppName() {
            return appName;
        }

        public OpenApiClusterGuardService.RateLimitDecision getRateLimitDecision() {
            return rateLimitDecision;
        }
    }
}
