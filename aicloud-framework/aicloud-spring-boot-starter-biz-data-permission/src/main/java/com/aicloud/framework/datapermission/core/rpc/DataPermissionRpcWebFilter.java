package com.aicloud.framework.datapermission.core.rpc;

import com.aicloud.framework.datapermission.core.aop.DataPermissionContextHolder;
import com.aicloud.framework.datapermission.core.util.DataPermissionUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * 针对 {@link DataPermissionRequestInterceptor} 的 RPC 调用，设置 {@link DataPermissionContextHolder} 的上下文
 *
 * @author 芋道源码
 */
public class DataPermissionRpcWebFilter extends OncePerRequestFilter {
    private final String internalSecret;

    public DataPermissionRpcWebFilter(String internalSecret) { this.internalSecret = internalSecret; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String enable = request.getHeader(DataPermissionRequestInterceptor.ENABLE_HEADER_NAME);
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String credential = request.getHeader("X-Aicloud-Internal-Token");
        boolean trustedRpc = path.startsWith("/rpc-api/") && internalSecret != null && !internalSecret.isBlank()
                && credential != null && java.security.MessageDigest.isEqual(
                internalSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), credential.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        if (trustedRpc && Objects.equals(enable, Boolean.FALSE.toString())) {
            DataPermissionUtils.executeIgnore(() -> {
                try {
                    chain.doFilter(request, response);
                } catch (IOException | ServletException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            chain.doFilter(request, response);
        }
    }

}
