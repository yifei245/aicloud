package com.aicloud.gateway.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
@ConfigurationProperties(prefix = "aicloud.gateway.openapi")
/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class OpenApiSecurityProperties {

    private boolean enabled = true;
    private long maxTimeDriftSeconds = 300;
    private long maxFutureSeconds = 30;
    private boolean replayProtectEnabled = true;
    private long nonceTtlSeconds = 300;
    private long minNonceWindowSeconds = 60;
    private boolean rateLimitEnabled = true;
    private int requestsPerMinute = 120;
    private boolean redisGuardEnabled = true;
    private List<App> apps = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getMaxTimeDriftSeconds() {
        return maxTimeDriftSeconds;
    }

    public void setMaxTimeDriftSeconds(long maxTimeDriftSeconds) {
        this.maxTimeDriftSeconds = maxTimeDriftSeconds;
    }

    public long getMaxFutureSeconds() {
        return maxFutureSeconds;
    }

    public void setMaxFutureSeconds(long maxFutureSeconds) {
        this.maxFutureSeconds = maxFutureSeconds;
    }

    public boolean isReplayProtectEnabled() {
        return replayProtectEnabled;
    }

    public void setReplayProtectEnabled(boolean replayProtectEnabled) {
        this.replayProtectEnabled = replayProtectEnabled;
    }

    public long getNonceTtlSeconds() {
        return nonceTtlSeconds;
    }

    public void setNonceTtlSeconds(long nonceTtlSeconds) {
        this.nonceTtlSeconds = nonceTtlSeconds;
    }

    public long getMinNonceWindowSeconds() {
        return minNonceWindowSeconds;
    }

    public void setMinNonceWindowSeconds(long minNonceWindowSeconds) {
        this.minNonceWindowSeconds = minNonceWindowSeconds;
    }

    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }

    public void setRateLimitEnabled(boolean rateLimitEnabled) {
        this.rateLimitEnabled = rateLimitEnabled;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public boolean isRedisGuardEnabled() {
        return redisGuardEnabled;
    }

    public void setRedisGuardEnabled(boolean redisGuardEnabled) {
        this.redisGuardEnabled = redisGuardEnabled;
    }

    public List<App> getApps() {
        return apps;
    }

    public void setApps(List<App> apps) {
        this.apps = apps == null ? new ArrayList<>() : apps;
    }

    public static class App {
        private Long tenantId = 1L;
        private String appKey;
        private String appSecret;
        private String appName;
        private String ipWhitelist;
        private boolean enabled = true;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public String getIpWhitelist() {
            return ipWhitelist;
        }

        public void setIpWhitelist(String ipWhitelist) {
            this.ipWhitelist = ipWhitelist;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
