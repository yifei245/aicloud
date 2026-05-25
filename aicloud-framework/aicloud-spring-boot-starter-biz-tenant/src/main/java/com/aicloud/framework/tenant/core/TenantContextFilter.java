package com.aicloud.framework.tenant.core;

import com.aicloud.framework.tenant.config.TenantProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Builds tenant context from gateway headers.
 *
 * @author yifei
 */
public class TenantContextFilter extends OncePerRequestFilter {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final TenantProperties tenantProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TenantContextFilter(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        TenantContext context = new TenantContext();
        context.setTenantId(resolveTenantId(request));
        context.setUserId(parseLong(request.getHeader(TenantConstants.HEADER_USER_ID)));
        context.setUsername(request.getHeader(TenantConstants.HEADER_USERNAME));
        context.setUserType(request.getHeader(TenantConstants.HEADER_USER_TYPE));
        context.setLoginTerminal(request.getHeader(TenantConstants.HEADER_LOGIN_TERMINAL));
        context.setRoles(parseJsonList(request.getHeader(TenantConstants.HEADER_USER_ROLES)));
        context.setPermissions(parseJsonList(request.getHeader(TenantConstants.HEADER_USER_PERMISSIONS)));
        context.setSuperAdmin(context.getRoles().stream()
                .anyMatch(role -> TenantConstants.ROLE_SUPER_ADMIN.equalsIgnoreCase(role)));
        context.setIgnoreTenant(context.isSuperAdmin()
                && Boolean.parseBoolean(request.getHeader(TenantConstants.HEADER_IGNORE_TENANT)));
        TenantContextHolder.setContext(context);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private Long resolveTenantId(HttpServletRequest request) {
        Long headerTenantId = parseLong(request.getHeader(TenantConstants.HEADER_TENANT_ID));
        if (headerTenantId != null) {
            return headerTenantId;
        }
        Long queryTenantId = parseLong(request.getParameter(TenantConstants.PARAM_TENANT_ID));
        if (queryTenantId != null) {
            return queryTenantId;
        }
        return tenantProperties.getDefaultTenantId();
    }

    private List<String> parseJsonList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(raw, STRING_LIST_TYPE);
        } catch (IOException ex) {
            return List.of();
        }
    }

    private Long parseLong(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
