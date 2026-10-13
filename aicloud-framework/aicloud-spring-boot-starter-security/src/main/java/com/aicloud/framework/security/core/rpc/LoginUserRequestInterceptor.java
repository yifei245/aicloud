package com.aicloud.framework.security.core.rpc;

import com.aicloud.framework.security.config.SecurityProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

import lombok.RequiredArgsConstructor;

/**
 * LoginUser 的 RequestInterceptor 实现类：Feign 请求时，将 {@link LoginUser} 设置到 header 中，继续透传给被调用的服务
 *
 * @author 芋道源码
 */
@Slf4j
@RequiredArgsConstructor
public class LoginUserRequestInterceptor implements RequestInterceptor {

    private final SecurityProperties securityProperties;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header(SecurityProperties.INTERNAL_TOKEN_HEADER, securityProperties.getInternalSecret());
        // Forward the original credential; callers cannot manufacture a LoginUser header.
        jakarta.servlet.http.HttpServletRequest request =
                com.aicloud.framework.common.util.servlet.ServletUtils.getRequest();
        if (request != null && request.getHeader("Authorization") != null) {
            requestTemplate.header("Authorization", request.getHeader("Authorization"));
        }
    }
}
