package com.aicloud.module.member.biz.controller;

import com.aicloud.module.member.biz.entity.AiMemberAccountLog;
import com.aicloud.module.member.biz.entity.AiMemberAddress;
import com.aicloud.module.member.biz.entity.AiMemberLevel;
import com.aicloud.module.member.biz.entity.AiMemberProfile;
import com.aicloud.common.pojo.ApiResponse;
import com.aicloud.module.member.biz.service.MemberProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "会员后台管理")
@RestController
@RequestMapping("/member/admin")
/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class MemberAdminController {
    private final MemberProfileService memberProfileService;

    public MemberAdminController(MemberProfileService memberProfileService) {
        this.memberProfileService = memberProfileService;
    }

    @Operation(summary = "会员列表")
    @GetMapping("/profile/list")
    public ApiResponse<List<AiMemberProfile>> profiles(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(memberProfileService.adminListProfiles(tenantId, keyword, status));
    }

    @Operation(summary = "保存会员资料")
    @PostMapping("/profile/save")
    public ApiResponse<AiMemberProfile> save(@RequestBody AiMemberProfile profile) {
        return ApiResponse.ok(memberProfileService.adminSaveProfile(profile));
    }

    @Operation(summary = "会员启停")
    @PostMapping("/profile/status")
    public ApiResponse<AiMemberProfile> status(@RequestParam Long id, @RequestParam Integer status) {
        return ApiResponse.ok(memberProfileService.adminUpdateStatus(id, status));
    }

    @Operation(summary = "会员等级")
    @GetMapping("/level/list")
    public ApiResponse<List<AiMemberLevel>> levels(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(memberProfileService.listLevels(tenantId));
    }

    @Operation(summary = "会员地址")
    @GetMapping("/address/list")
    public ApiResponse<List<AiMemberAddress>> addresses(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "userId", required = false) Long userId) {
        return ApiResponse.ok(memberProfileService.adminListAddresses(tenantId, userId));
    }

    @Operation(summary = "会员账户流水")
    @GetMapping("/account-log/list")
    public ApiResponse<List<AiMemberAccountLog>> accountLogs(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "userId", required = false) Long userId) {
        return ApiResponse.ok(memberProfileService.adminListAccountLogs(tenantId, userId));
    }
}
