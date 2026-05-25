package com.aicloud.framework.tenant.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tenant framework properties.
 *
 * @author yifei
 */
@ConfigurationProperties(prefix = "aicloud.tenant")
public class TenantProperties {

    private boolean enabled = true;
    private Long defaultTenantId = 1L;
    private List<String> ignoreTables = new ArrayList<>();

    public TenantProperties() {
        ignoreTables.add("ai_tenant");
        ignoreTables.add("ai_login_log");
        ignoreTables.add("ai_sso_session");
        ignoreTables.add("ai_oauth2_client");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Long getDefaultTenantId() {
        return defaultTenantId;
    }

    public void setDefaultTenantId(Long defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }

    public List<String> getIgnoreTables() {
        return ignoreTables;
    }

    public void setIgnoreTables(List<String> ignoreTables) {
        this.ignoreTables = ignoreTables == null ? new ArrayList<>() : ignoreTables;
    }
}
