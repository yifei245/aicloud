package com.aicloud.module.member.biz.controller;

import com.aicloud.module.member.biz.entity.AiMemberAddress;
import com.aicloud.module.member.biz.entity.AiMemberLevel;
import com.aicloud.module.member.biz.model.ApiResponse;
import com.aicloud.module.member.biz.model.address.MemberAddressSaveRequest;
import com.aicloud.module.member.biz.model.common.TerminalUserContext;
import com.aicloud.module.member.biz.model.profile.MemberProfileResponse;
import com.aicloud.module.member.biz.model.profile.MemberProfileUpdateRequest;
import com.aicloud.module.member.biz.service.MemberAccountService;
import com.aicloud.module.member.biz.service.MemberProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "会员中心")
@RestController
@RequestMapping
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class MemberProfileController {

    private static final String USER_TYPE_MEMBER = "MEMBER";

    private final MemberProfileService memberProfileService;
    private final MemberAccountService memberAccountService;

    public MemberProfileController(MemberProfileService memberProfileService, MemberAccountService memberAccountService) {
        this.memberProfileService = memberProfileService;
        this.memberAccountService = memberAccountService;
    }

    @Operation(summary = "APP 端会员信息")
    @GetMapping("/app/member/profile")
    public ApiResponse<MemberProfileResponse> appProfile(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(name = "X-Username", required = false) String username,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal) {
        TerminalUserContext context = parseAndValidate(tenantIdHeader, userIdHeader, username, userType, terminal, "APP");
        return ApiResponse.ok(memberProfileService.getProfile(context));
    }

    @Operation(summary = "WEB 端会员信息")
    @GetMapping("/web/member/profile")
    public ApiResponse<MemberProfileResponse> webProfile(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(name = "X-Username", required = false) String username,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal) {
        TerminalUserContext context = parseAndValidate(tenantIdHeader, userIdHeader, username, userType, terminal, "WEB");
        return ApiResponse.ok(memberProfileService.getProfile(context));
    }

    @Operation(summary = "更新会员资料")
    @PutMapping({"/app/member/profile", "/web/member/profile"})
    public ApiResponse<MemberProfileResponse> updateProfile(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(name = "X-Username", required = false) String username,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal,
            @RequestBody MemberProfileUpdateRequest request) {
        TerminalUserContext context = parseAndValidate(tenantIdHeader, userIdHeader, username, userType, terminal, null);
        return ApiResponse.ok(memberProfileService.updateProfile(context, request));
    }

    @Operation(summary = "会员等级列表")
    @GetMapping({"/app/member/levels", "/web/member/levels"})
    public ApiResponse<List<AiMemberLevel>> levels(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(name = "X-Username", required = false) String username,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal) {
        TerminalUserContext context = parseAndValidate(tenantIdHeader, userIdHeader, username, userType, terminal, null);
        return ApiResponse.ok(memberProfileService.listLevels(context.getTenantId()));
    }

    @Operation(summary = "会员地址列表")
    @GetMapping({"/app/member/address/list", "/web/member/address/list"})
    public ApiResponse<List<AiMemberAddress>> addresses(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(name = "X-Username", required = false) String username,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal) {
        TerminalUserContext context = parseAndValidate(tenantIdHeader, userIdHeader, username, userType, terminal, null);
        return ApiResponse.ok(memberProfileService.listAddresses(context));
    }

    @Operation(summary = "保存会员地址")
    @PostMapping({"/app/member/address/save", "/web/member/address/save"})
    public ApiResponse<AiMemberAddress> saveAddress(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(name = "X-Username", required = false) String username,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal,
            @RequestBody MemberAddressSaveRequest request) {
        TerminalUserContext context = parseAndValidate(tenantIdHeader, userIdHeader, username, userType, terminal, null);
        return ApiResponse.ok(memberProfileService.saveAddress(context, request));
    }

    @Operation(summary = "删除会员地址")
    @DeleteMapping({"/app/member/address/{id}", "/web/member/address/{id}"})
    public ApiResponse<Boolean> deleteAddress(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(name = "X-Username", required = false) String username,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal,
            @PathVariable("id") Long id) {
        TerminalUserContext context = parseAndValidate(tenantIdHeader, userIdHeader, username, userType, terminal, null);
        memberProfileService.deleteAddress(context, id);
        return ApiResponse.ok(true);
    }

    @Operation(summary = "会员账户流水")
    @GetMapping({"/app/member/account/logs", "/web/member/account/logs"})
    public ApiResponse<List<com.aicloud.module.member.biz.entity.AiMemberAccountLog>> accountLogs(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
            @RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
            @RequestHeader(name = "X-Username", required = false) String username,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestHeader(name = "X-Login-Terminal", required = false) String terminal,
            @RequestParam(name = "bizType", required = false) String bizType) {
        TerminalUserContext context = parseAndValidate(tenantIdHeader, userIdHeader, username, userType, terminal, null);
        return ApiResponse.ok(memberAccountService.listLogs(context, bizType));
    }

    private TerminalUserContext parseAndValidate(String tenantIdHeader, String userIdHeader, String username,
                                                 String userType, String terminal, String expectedTerminal) {
        if (!USER_TYPE_MEMBER.equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("仅会员用户可访问");
        }
        if (expectedTerminal != null && !expectedTerminal.equalsIgnoreCase(terminal)) {
            throw new IllegalArgumentException("仅 " + expectedTerminal + " 终端可访问");
        }
        TerminalUserContext context = new TerminalUserContext();
        context.setTenantId(parseLong(tenantIdHeader, 1L));
        context.setUserId(parseLong(userIdHeader, null));
        context.setUsername(username);
        context.setUserType(userType);
        context.setTerminal(terminal);
        if (context.getUserId() == null) {
            throw new IllegalArgumentException("缺少用户上下文");
        }
        return context;
    }

    private Long parseLong(String val, Long defaultValue) {
        if (val == null || val.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
