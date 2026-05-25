package com.aicloud.framework.web.audit;

import com.aicloud.framework.tenant.core.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Request audit filter.
 *
 * @author yifei
 */
public class AuditLogFilter extends OncePerRequestFilter {

    private final AuditLogPublisher auditLogPublisher;

    public AuditLogFilter(AuditLogPublisher auditLogPublisher) {
        this.auditLogPublisher = auditLogPublisher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        Exception error = null;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            error = ex;
            throw ex;
        } finally {
            publish(request, response, start, error);
        }
    }

    private void publish(HttpServletRequest request, HttpServletResponse response, long start, Exception error) {
        String uri = request.getRequestURI();
        if (isStaticOrDoc(uri)) {
            return;
        }
        AuditLogEvent event = new AuditLogEvent();
        event.setTenantId(TenantContextHolder.getTenantId());
        event.setUserId(TenantContextHolder.getUserId());
        event.setUsername(TenantContextHolder.getUsername());
        event.setModule(resolveModule(uri));
        event.setOperation(request.getMethod() + " " + uri);
        event.setRequestUri(uri);
        event.setRequestMethod(request.getMethod());
        event.setRequestIp(resolveIp(request));
        event.setSuccess(error == null && response.getStatus() < 400 ? 1 : 0);
        event.setErrorMsg(error == null ? null : error.getClass().getSimpleName());
        event.setCostMillis(System.currentTimeMillis() - start);
        event.setCreateTime(LocalDateTime.now());
        auditLogPublisher.publish(event);
    }

    private boolean isStaticOrDoc(String uri) {
        return uri == null
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/doc.html")
                || uri.startsWith("/webjars")
                || uri.equals("/favicon.ico");
    }

    private String resolveModule(String uri) {
        if (!StringUtils.hasText(uri)) {
            return "unknown";
        }
        String[] parts = uri.split("/");
        return parts.length > 1 && StringUtils.hasText(parts[1]) ? parts[1] : "root";
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int idx = xff.indexOf(',');
            return idx > 0 ? xff.substring(0, idx).trim() : xff.trim();
        }
        return request.getRemoteAddr();
    }
}
