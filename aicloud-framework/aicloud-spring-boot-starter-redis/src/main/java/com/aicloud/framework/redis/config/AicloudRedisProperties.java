package com.aicloud.framework.redis.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis framework properties.
 *
 * @author yifei
 */
@ConfigurationProperties(prefix = "aicloud.redis")
public class AicloudRedisProperties {

    private boolean enabled = true;
    private String keyPrefix = "aicloud";
    private Duration defaultTtl = Duration.ofHours(2);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(Duration defaultTtl) {
        this.defaultTtl = defaultTtl;
    }
}
