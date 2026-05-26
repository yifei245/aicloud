package com.aicloud.framework.biz.auth.core;

import com.aicloud.framework.security.core.SecurityContext;
import com.aicloud.framework.security.core.SecurityUser;
import java.util.List;

/**
 * Shared business authentication facade.
 *
 * @author yifei
 */
public class BizAuthService {

    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    public SecurityUser getLoginUser() {
        return SecurityContext.getLoginUser();
    }

    public SecurityUser requireLogin() {
        SecurityUser user = getLoginUser();
        if (user == null || user.getUserId() == null) {
            throw new BizAuthException(UNAUTHORIZED, "用户未登录");
        }
        return user;
    }

    public Long requireTenantId() {
        SecurityUser user = requireLogin();
        if (user.getTenantId() == null) {
            throw new BizAuthException(FORBIDDEN, "缺少租户上下文");
        }
        return user.getTenantId();
    }

    public void requirePermission(String permission) {
        if (!SecurityContext.hasPermission(permission)) {
            throw new BizAuthException(FORBIDDEN, "权限不足");
        }
    }

    public void requireAnyPermission(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }
        boolean matched = permissions.stream().anyMatch(SecurityContext::hasPermission);
        if (!matched) {
            throw new BizAuthException(FORBIDDEN, "权限不足");
        }
    }

    public void requireAdmin() {
        SecurityUser user = requireLogin();
        boolean admin = user.isSuperAdmin()
                || user.getRoles().stream().anyMatch(role -> ROLE_ADMIN.equalsIgnoreCase(role)
                || ROLE_SUPER_ADMIN.equalsIgnoreCase(role));
        if (!admin) {
            throw new BizAuthException(FORBIDDEN, "需要管理员角色");
        }
    }

    public boolean isSuperAdmin() {
        SecurityUser user = getLoginUser();
        return user != null && user.isSuperAdmin();
    }
}
