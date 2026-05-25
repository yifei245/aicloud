package com.aicloud.module.openapi.biz.controller;

import com.aicloud.module.openapi.biz.entity.AiMemberAccountLog;
import com.aicloud.common.pojo.ApiResponse;
import com.aicloud.module.openapi.biz.service.OpenApiMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Tag(name = "第三方开放接口")
@RestController
public class OpenApiDemoController {

    private final OpenApiMemberService openApiMemberService;

    public OpenApiDemoController(OpenApiMemberService openApiMemberService) {
        this.openApiMemberService = openApiMemberService;
    }

    @Operation(summary = "开放平台连通性检查")
    @GetMapping("/openapi/v1/ping")
    public ApiResponse<Map<String, Object>> ping(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-App-Key", required = false) String appKey) {
        Map<String, Object> data = new HashMap<>(8);
        data.put("tenantId", tenantId);
        data.put("appKey", appKey);
        data.put("serverTime", LocalDateTime.now());
        data.put("message", "openapi gateway signature auth ok");
        return ApiResponse.ok(data);
    }

    @Operation(summary = "开放平台会员摘要查询")
    @GetMapping("/openapi/v1/member/summary")
    public ApiResponse<Map<String, Object>> memberSummary(
            @RequestParam("memberId") Long memberId,
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-App-Key", required = false) String appKey) {
        return ApiResponse.ok(openApiMemberService.summary(parseLong(tenantId, 1L), memberId, appKey));
    }

    @Operation(summary = "开放平台会员详情查询")
    @GetMapping("/openapi/v1/member/detail")
    public ApiResponse<Map<String, Object>> memberDetail(
            @RequestParam("memberId") Long memberId,
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-App-Key", required = false) String appKey) {
        return ApiResponse.ok(openApiMemberService.detail(parseLong(tenantId, 1L), memberId, appKey));
    }

    @Operation(summary = "开放平台会员账户流水查询")
    @GetMapping("/openapi/v1/member/account/logs")
    public ApiResponse<List<AiMemberAccountLog>> accountLogs(
            @RequestParam("memberId") Long memberId,
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId) {
        return ApiResponse.ok(openApiMemberService.accountLogs(parseLong(tenantId, 1L), memberId));
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
