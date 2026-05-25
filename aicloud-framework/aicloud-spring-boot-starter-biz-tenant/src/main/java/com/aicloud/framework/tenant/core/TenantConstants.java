package com.aicloud.framework.tenant.core;

/**
 * Tenant header constants.
 *
 * @author yifei
 */
public final class TenantConstants {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_USER_TYPE = "X-User-Type";
    public static final String HEADER_LOGIN_TERMINAL = "X-Login-Terminal";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";
    public static final String HEADER_IGNORE_TENANT = "X-Ignore-Tenant";
    public static final String PARAM_TENANT_ID = "tenantId";
    public static final Long DEFAULT_TENANT_ID = 1L;
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    private TenantConstants() {
    }
}
