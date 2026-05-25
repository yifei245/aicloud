package com.aicloud.module.mp.biz.controller;

import com.aicloud.module.mp.biz.entity.AiMpUserBind;
import com.aicloud.common.pojo.ApiResponse;
import com.aicloud.module.mp.biz.model.MpBindRequest;
import com.aicloud.module.mp.biz.service.MpUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Tag(name = "小程序用户")
@RestController
public class MpUserController {

    private static final String USER_TYPE_MEMBER = "MEMBER";
    private static final String LOGIN_TERMINAL_MP = "MP";

    private final MpUserService mpUserService;

    public MpUserController(MpUserService mpUserService) {
        this.mpUserService = mpUserService;
    }

    @Operation(summary = "小程序端用户信息")
    @GetMapping("/mp/user/profile")
    public ApiResponse<Map<String, Object>> profile(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-Username", required = false) String username,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal) {
        validate(userType, terminal);
        return ApiResponse.ok(mpUserService.profile(parseLong(tenantId, 1L), parseLong(userId, null), username));
    }

    @Operation(summary = "绑定小程序账号")
    @PostMapping("/mp/user/bind")
    public ApiResponse<AiMpUserBind> bind(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal,
            @RequestBody MpBindRequest request) {
        validate(userType, terminal);
        return ApiResponse.ok(mpUserService.bind(parseLong(tenantId, 1L), parseLong(userId, null), request));
    }

    @Operation(summary = "解绑小程序账号")
    @DeleteMapping("/mp/user/unbind")
    public ApiResponse<Boolean> unbind(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal) {
        validate(userType, terminal);
        mpUserService.unbind(parseLong(tenantId, 1L), parseLong(userId, null));
        return ApiResponse.ok(true);
    }

    private void validate(String userType, String terminal) {
        if (!USER_TYPE_MEMBER.equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("仅会员用户可访问");
        }
        if (!LOGIN_TERMINAL_MP.equalsIgnoreCase(terminal)) {
            throw new IllegalArgumentException("仅 MP 终端可访问");
        }
    }

    private Long parseLong(String value, Long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
