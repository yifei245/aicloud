package com.aicloud.framework.security.core;

import com.aicloud.framework.tenant.core.TenantContext;
import com.aicloud.framework.tenant.core.TenantContextHolder;

/**
 * Security context facade.
 *
 * @author yifei
 */
public final class SecurityContext {

    private SecurityContext() {
    }

    public static SecurityUser getLoginUser() {
        TenantContext context = TenantContextHolder.getContext();
        if (context == null) {
            return null;
        }
        SecurityUser user = new SecurityUser();
        user.setTenantId(context.getTenantId());
        user.setUserId(context.getUserId());
        user.setUsername(context.getUsername());
        user.setUserType(context.getUserType());
        user.setLoginTerminal(context.getLoginTerminal());
        user.setRoles(context.getRoles());
        user.setPermissions(context.getPermissions());
        user.setSuperAdmin(context.isSuperAdmin());
        return user;
    }

    public static boolean hasPermission(String permission) {
        SecurityUser user = getLoginUser();
        return user != null && (user.isSuperAdmin() || user.getPermissions().contains(permission));
    }

    public static boolean hasRole(String role) {
        SecurityUser user = getLoginUser();
        return user != null && user.getRoles().stream().anyMatch(item -> item.equalsIgnoreCase(role));
    }
}
