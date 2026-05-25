package com.aicloud.framework.tenant.core;

import java.util.List;

/**
 * Thread local holder for tenant context.
 *
 * @author yifei
 */
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void setContext(TenantContext context) {
        CONTEXT_HOLDER.set(context);
    }

    public static TenantContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    public static Long getTenantId() {
        TenantContext context = getContext();
        return context == null || context.getTenantId() == null
                ? TenantConstants.DEFAULT_TENANT_ID
                : context.getTenantId();
    }

    public static Long getUserId() {
        TenantContext context = getContext();
        return context == null ? null : context.getUserId();
    }

    public static String getUsername() {
        TenantContext context = getContext();
        return context == null ? null : context.getUsername();
    }

    public static List<String> getPermissions() {
        TenantContext context = getContext();
        return context == null ? List.of() : context.getPermissions();
    }

    public static boolean isSuperAdmin() {
        TenantContext context = getContext();
        return context != null && context.isSuperAdmin();
    }

    public static boolean isTenantIgnored() {
        TenantContext context = getContext();
        return context != null && context.isSuperAdmin() && context.isIgnoreTenant();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
