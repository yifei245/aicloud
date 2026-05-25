package com.aicloud.module.mp.biz.controller;

import com.aicloud.module.mp.biz.entity.AiMpMaterial;
import com.aicloud.module.mp.biz.entity.AiMpMenu;
import com.aicloud.module.mp.biz.entity.AiMpMessageLog;
import com.aicloud.module.mp.biz.entity.AiMpMessageTemplate;
import com.aicloud.common.pojo.ApiResponse;
import com.aicloud.module.mp.biz.service.MpPlatformService;
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

@Tag(name = "小程序平台管理")
@RestController
/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class MpPlatformController {

    private final MpPlatformService mpPlatformService;

    public MpPlatformController(MpPlatformService mpPlatformService) {
        this.mpPlatformService = mpPlatformService;
    }

    @Operation(summary = "小程序菜单列表")
    @GetMapping("/mp/menu/list")
    public ApiResponse<List<AiMpMenu>> listMenus(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId) {
        return ApiResponse.ok(mpPlatformService.listMenus(parseLong(tenantId, 1L)));
    }

    @Operation(summary = "保存小程序菜单")
    @PostMapping("/mp/menu/save")
    public ApiResponse<AiMpMenu> saveMenu(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestBody AiMpMenu menu) {
        return ApiResponse.ok(mpPlatformService.saveMenu(parseLong(tenantId, 1L), menu));
    }

    @Operation(summary = "发布小程序菜单")
    @PostMapping("/mp/menu/publish")
    public ApiResponse<Map<String, Object>> publishMenu(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId) {
        return ApiResponse.ok(mpPlatformService.publishMenu(parseLong(tenantId, 1L)));
    }

    @Operation(summary = "小程序素材列表")
    @GetMapping("/mp/material/list")
    public ApiResponse<List<AiMpMaterial>> listMaterials(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestParam(name = "materialType", required = false) String materialType) {
        return ApiResponse.ok(mpPlatformService.listMaterials(parseLong(tenantId, 1L), materialType));
    }

    @Operation(summary = "保存小程序素材")
    @PostMapping("/mp/material/save")
    public ApiResponse<AiMpMaterial> saveMaterial(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestBody AiMpMaterial material) {
        return ApiResponse.ok(mpPlatformService.saveMaterial(parseLong(tenantId, 1L), material));
    }

    @Operation(summary = "小程序消息模板列表")
    @GetMapping("/mp/message/template/list")
    public ApiResponse<List<AiMpMessageTemplate>> listTemplates(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId) {
        return ApiResponse.ok(mpPlatformService.listTemplates(parseLong(tenantId, 1L)));
    }

    @Operation(summary = "保存小程序消息模板")
    @PostMapping("/mp/message/template/save")
    public ApiResponse<AiMpMessageTemplate> saveTemplate(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestBody AiMpMessageTemplate template) {
        return ApiResponse.ok(mpPlatformService.saveTemplate(parseLong(tenantId, 1L), template));
    }

    @Operation(summary = "发送小程序模板消息")
    @PostMapping("/mp/message/send")
    public ApiResponse<AiMpMessageLog> sendMessage(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestParam("userId") Long userId,
            @RequestParam("templateCode") String templateCode,
            @RequestParam(name = "content", required = false) String content) {
        return ApiResponse.ok(mpPlatformService.sendMessage(parseLong(tenantId, 1L), userId, templateCode, content));
    }

    @Operation(summary = "小程序消息发送日志")
    @GetMapping("/mp/message/log/list")
    public ApiResponse<List<AiMpMessageLog>> listMessageLogs(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestParam(name = "userId", required = false) Long userId) {
        return ApiResponse.ok(mpPlatformService.listMessageLogs(parseLong(tenantId, 1L), userId));
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
