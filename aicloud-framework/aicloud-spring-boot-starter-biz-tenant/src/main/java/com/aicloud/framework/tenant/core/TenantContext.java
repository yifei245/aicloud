package com.aicloud.framework.tenant.core;

import java.util.List;

/**
 * Per-request tenant and user context.
 *
 * @author yifei
 */
public class TenantContext {

    private Long tenantId;
    private Long userId;
    private String username;
    private String userType;
    private String loginTerminal;
    private List<String> roles = List.of();
    private List<String> permissions = List.of();
    private boolean superAdmin;
    private boolean ignoreTenant;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLoginTerminal() {
        return loginTerminal;
    }

    public void setLoginTerminal(String loginTerminal) {
        this.loginTerminal = loginTerminal;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles == null ? List.of() : roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions == null ? List.of() : permissions;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    public boolean isIgnoreTenant() {
        return ignoreTenant;
    }

    public void setIgnoreTenant(boolean ignoreTenant) {
        this.ignoreTenant = ignoreTenant;
    }
}
