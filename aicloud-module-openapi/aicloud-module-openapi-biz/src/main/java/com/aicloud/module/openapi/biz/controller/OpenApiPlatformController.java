package com.aicloud.module.openapi.biz.controller;

import com.aicloud.module.openapi.biz.entity.AiOpenApiApp;
import com.aicloud.module.openapi.biz.entity.AiOpenApiCallLog;
import com.aicloud.module.openapi.biz.entity.AiOpenApiWebhook;
import com.aicloud.module.openapi.biz.model.ApiResponse;
import com.aicloud.module.openapi.biz.service.OpenApiPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "开放平台管理")
@RestController
public class OpenApiPlatformController {

    private final OpenApiPlatformService platformService;

    public OpenApiPlatformController(OpenApiPlatformService platformService) {
        this.platformService = platformService;
    }

    @Operation(summary = "开放应用列表")
    @GetMapping("/openapi/app/list")
    public ApiResponse<List<AiOpenApiApp>> listApps(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponse.ok(platformService.listApps(parseLong(tenantId, 1L), keyword));
    }

    @Operation(summary = "保存开放应用")
    @PostMapping("/openapi/app/save")
    public ApiResponse<AiOpenApiApp> saveApp(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestBody AiOpenApiApp app) {
        return ApiResponse.ok(platformService.saveApp(parseLong(tenantId, 1L), app));
    }

    @Operation(summary = "启停开放应用")
    @PostMapping("/openapi/app/status")
    public ApiResponse<Boolean> updateAppStatus(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestParam("id") Long id,
            @RequestParam("status") Integer status) {
        platformService.updateAppStatus(parseLong(tenantId, 1L), id, status);
        return ApiResponse.ok(true);
    }

    @Operation(summary = "开放接口调用日志")
    @GetMapping("/openapi/log/list")
    public ApiResponse<List<AiOpenApiCallLog>> listLogs(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestParam(name = "appKey", required = false) String appKey,
            @RequestParam(name = "apiPath", required = false) String apiPath) {
        return ApiResponse.ok(platformService.listLogs(parseLong(tenantId, 1L), appKey, apiPath));
    }

    @Operation(summary = "Webhook 列表")
    @GetMapping("/openapi/webhook/list")
    public ApiResponse<List<AiOpenApiWebhook>> listWebhooks(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestParam(name = "appKey", required = false) String appKey) {
        return ApiResponse.ok(platformService.listWebhooks(parseLong(tenantId, 1L), appKey));
    }

    @Operation(summary = "保存 Webhook")
    @PostMapping("/openapi/webhook/save")
    public ApiResponse<AiOpenApiWebhook> saveWebhook(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestBody AiOpenApiWebhook webhook) {
        return ApiResponse.ok(platformService.saveWebhook(parseLong(tenantId, 1L), webhook));
    }

    @Operation(summary = "测试 Webhook 推送")
    @PostMapping("/openapi/webhook/test")
    public ApiResponse<Map<String, Object>> testWebhook(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestParam("id") Long id) {
        return ApiResponse.ok(platformService.testWebhook(parseLong(tenantId, 1L), id));
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
