package com.aicloud.auth.controller;

import com.aicloud.auth.model.ApiResponse;
import com.aicloud.auth.model.LoginRequest;
import com.aicloud.auth.model.OnlineSessionResponse;
import com.aicloud.auth.model.RefreshTokenRequest;
import com.aicloud.auth.model.TokenResponse;
import com.aicloud.auth.model.TokenVerifyResponse;
import com.aicloud.auth.service.TokenStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Tag(name = "认证中心")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenStoreService tokenStoreService;

    public AuthController(TokenStoreService tokenStoreService) {
        this.tokenStoreService = tokenStoreService;
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        TokenResponse tokenResponse = tokenStoreService.login(
                request.getTenantId(), request.getUsername(), request.getPassword(), request.getTerminal(), servletRequest.getRemoteAddr());
        if (tokenResponse == null) {
            return ApiResponse.fail(4001, "用户名或密码错误");
        }
        return ApiResponse.ok(tokenResponse);
    }

    @Operation(summary = "SSO 令牌获取")
    @PostMapping("/sso/token")
    public ApiResponse<TokenResponse> ssoToken(@RequestBody Map<String, String> body, HttpServletRequest servletRequest) {
        String clientId = body.get("clientId");
        String clientSecret = body.get("clientSecret");
        Long tenantId = parseTenantId(body.get("tenantId"));
        if (!tokenStoreService.verifyClient(tenantId, clientId, clientSecret)) {
            return ApiResponse.fail(4003, "客户端校验失败");
        }
        TokenResponse tokenResponse = tokenStoreService.login(
                tenantId, body.get("username"), body.get("password"), body.get("terminal"), servletRequest.getRemoteAddr());
        if (tokenResponse == null) {
            return ApiResponse.fail(4001, "用户名或密码错误");
        }
        return ApiResponse.ok(tokenResponse);
    }

    @Operation(summary = "刷新 Access Token")
    @PostMapping("/token/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        TokenResponse tokenResponse = tokenStoreService.refresh(request.getRefreshToken());
        if (tokenResponse == null) {
            return ApiResponse.fail(4005, "refresh token 无效或已过期");
        }
        return ApiResponse.ok(tokenResponse);
    }

    @Operation(summary = "Token 校验（网关内部调用）")
    @GetMapping("/token/verify")
    public ApiResponse<TokenVerifyResponse> verify(@RequestParam("token") String token) {
        return ApiResponse.ok(tokenStoreService.verify(token));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            tokenStoreService.logout(authHeader.substring(7));
        }
        return ApiResponse.ok(true);
    }

    @Operation(summary = "获取当前登录信息")
    @GetMapping("/me")
    public ApiResponse<TokenVerifyResponse> me(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return ApiResponse.fail(4004, "缺少 Authorization");
        }
        return ApiResponse.ok(tokenStoreService.verify(authHeader.substring(7)));
    }

    @Operation(summary = "SSO 踢出指定用户全部会话")
    @PostMapping("/sso/kickout")
    public ApiResponse<Integer> kickout(@RequestParam("userId") Long userId,
                                        @RequestHeader(name = "Authorization", required = false) String authHeader,
                                        @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader) {
        if (!isAdmin(authHeader)) {
            return ApiResponse.fail(403, "需要管理员权限");
        }
        Long tenantId = parseTenantId(tenantIdHeader);
        return ApiResponse.ok(tokenStoreService.kickoutUserSessions(tenantId, userId));
    }

    @Operation(summary = "按会话ID强制下线")
    @PostMapping("/sso/kickout/session")
    public ApiResponse<Integer> kickoutBySessionId(@RequestParam("sessionId") String sessionId,
                                                   @RequestHeader(name = "Authorization", required = false) String authHeader,
                                                   @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader) {
        if (!isAdmin(authHeader)) {
            return ApiResponse.fail(403, "需要管理员权限");
        }
        Long tenantId = parseTenantId(tenantIdHeader);
        return ApiResponse.ok(tokenStoreService.kickoutSession(tenantId, sessionId));
    }

    @Operation(summary = "在线会话列表")
    @GetMapping("/sso/sessions")
    public ApiResponse<List<OnlineSessionResponse>> listOnlineSessions(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader) {
        if (!isAdmin(authHeader)) {
            return ApiResponse.fail(403, "需要管理员权限");
        }
        Long tenantId = parseTenantId(tenantIdHeader);
        return ApiResponse.ok(tokenStoreService.listOnlineSessions(tenantId, userId));
    }

    @Operation(summary = "权限点校验")
    @GetMapping("/permission/check")
    public ApiResponse<Boolean> checkPermission(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @RequestParam("permission") String permission) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return ApiResponse.fail(4004, "缺少 Authorization");
        }
        TokenVerifyResponse verifyResponse = tokenStoreService.verify(authHeader.substring(7));
        if (!verifyResponse.isValid()) {
            return ApiResponse.fail(401, "token 无效");
        }
        List<String> permissions = verifyResponse.getPermissions();
        return ApiResponse.ok(permissions != null && permissions.contains(permission));
    }

    private Long parseTenantId(String tenantIdValue) {
        if (tenantIdValue == null || tenantIdValue.isBlank()) {
            return 1L;
        }
        try {
            return Long.parseLong(tenantIdValue);
        } catch (NumberFormatException ex) {
            return 1L;
        }
    }

    private boolean isAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return false;
        }
        TokenVerifyResponse verifyResponse = tokenStoreService.verify(authHeader.substring(7));
        if (!verifyResponse.isValid() || verifyResponse.getRoles() == null) {
            return false;
        }
        return verifyResponse.getRoles().contains("SUPER_ADMIN")
                || verifyResponse.getRoles().contains("ADMIN");
    }
}
