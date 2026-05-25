package com.aicloud.module.system.biz.controller;

import com.aicloud.module.system.biz.annotation.RequirePermission;
import com.aicloud.module.system.biz.entity.AiSystemConfig;
import com.aicloud.module.system.biz.model.ApiResponse;
import com.aicloud.module.system.biz.model.config.SystemConfigSaveRequest;
import com.aicloud.module.system.biz.service.SystemConfigAdminService;
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

@Tag(name = "系统参数")
@RestController
@RequestMapping("/system/config")
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class SystemConfigController {

    private final SystemConfigAdminService systemConfigAdminService;

    public SystemConfigController(SystemConfigAdminService systemConfigAdminService) {
        this.systemConfigAdminService = systemConfigAdminService;
    }

    @Operation(summary = "参数列表")
    @RequirePermission("system:config:query")
    @GetMapping("/list")
    public ApiResponse<List<AiSystemConfig>> list(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                                  @RequestParam(name = "keyword", required = false) String keyword,
                                                  @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(systemConfigAdminService.list(parseTenantId(tenantIdHeader), keyword, status));
    }

    @Operation(summary = "保存参数")
    @RequirePermission("system:config:update")
    @PostMapping("/save")
    public ApiResponse<AiSystemConfig> save(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                            @RequestBody SystemConfigSaveRequest request) {
        return ApiResponse.ok(systemConfigAdminService.save(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "删除参数")
    @RequirePermission("system:config:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                       @PathVariable("id") Long id) {
        systemConfigAdminService.delete(parseTenantId(tenantIdHeader), id);
        return ApiResponse.ok(true);
    }

    private Long parseTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return 1L;
        }
        return Long.parseLong(tenantIdHeader);
    }
}
