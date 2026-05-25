package com.aicloud.module.system.biz.controller;

import com.aicloud.module.system.biz.annotation.RequirePermission;
import com.aicloud.module.system.biz.entity.AiTenant;
import com.aicloud.module.system.biz.model.ApiResponse;
import com.aicloud.module.system.biz.service.TenantAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统租户")
@RestController
@RequestMapping("/system/tenant")
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class TenantController {

    private final TenantAdminService tenantAdminService;

    public TenantController(TenantAdminService tenantAdminService) {
        this.tenantAdminService = tenantAdminService;
    }

    @Operation(summary = "租户列表")
    @RequirePermission("system:user:query")
    @GetMapping("/list")
    public ApiResponse<List<AiTenant>> list() {
        return ApiResponse.ok(tenantAdminService.listEnabled());
    }
}
