package com.aicloud.framework.mq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MQ framework properties.
 *
 * @author yifei
 */
@ConfigurationProperties(prefix = "aicloud.mq")
public class AicloudMqProperties {

    private boolean enabled = true;
    private String defaultTopic = "aicloud.domain.event";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultTopic() {
        return defaultTopic;
    }

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }
}
