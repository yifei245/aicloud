package com.aicloud.framework.openapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAPI framework properties.
 *
 * @author yifei
 */
@ConfigurationProperties(prefix = "aicloud.openapi")
public class AicloudOpenApiProperties {

    private boolean enabled = true;
    private String title = "AICloud API";
    private String description = "AICloud service API";
    private String version = "1.0.0";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
