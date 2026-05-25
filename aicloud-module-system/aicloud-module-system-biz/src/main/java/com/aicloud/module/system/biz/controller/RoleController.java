package com.aicloud.module.system.biz.controller;

import com.aicloud.framework.rbac.annotation.RequirePermission;
import com.aicloud.common.pojo.ApiResponse;
import com.aicloud.module.system.biz.model.role.RoleResponse;
import com.aicloud.module.system.biz.model.role.RoleSaveRequest;
import com.aicloud.module.system.biz.model.role.RoleStatusRequest;
import com.aicloud.module.system.biz.service.RoleAdminService;
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

@Tag(name = "系统角色")
@RestController
@RequestMapping("/system/role")
/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class RoleController {

    private final RoleAdminService roleAdminService;

    public RoleController(RoleAdminService roleAdminService) {
        this.roleAdminService = roleAdminService;
    }

    @Operation(summary = "角色列表")
    @RequirePermission("system:role:query")
    @GetMapping("/list")
    public ApiResponse<List<RoleResponse>> list(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                                @RequestParam(name = "keyword", required = false) String keyword,
                                                @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(roleAdminService.list(parseTenantId(tenantIdHeader), keyword, status));
    }

    @Operation(summary = "角色详情")
    @RequirePermission("system:role:query")
    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> get(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                         @PathVariable("id") Long id) {
        return ApiResponse.ok(roleAdminService.get(parseTenantId(tenantIdHeader), id));
    }

    @Operation(summary = "创建角色")
    @RequirePermission("system:role:create")
    @PostMapping("/create")
    public ApiResponse<RoleResponse> create(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                            @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(roleAdminService.create(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "更新角色")
    @RequirePermission("system:role:update")
    @PutMapping("/update")
    public ApiResponse<RoleResponse> update(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                            @RequestBody RoleSaveRequest request) {
        return ApiResponse.ok(roleAdminService.update(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "更新角色状态")
    @RequirePermission("system:role:update")
    @PutMapping("/status")
    public ApiResponse<Boolean> updateStatus(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                             @RequestBody RoleStatusRequest request) {
        roleAdminService.updateStatus(parseTenantId(tenantIdHeader), request.getId(), request.getStatus());
        return ApiResponse.ok(true);
    }

    @Operation(summary = "删除角色")
    @RequirePermission("system:role:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                       @PathVariable("id") Long id) {
        roleAdminService.delete(parseTenantId(tenantIdHeader), id);
        return ApiResponse.ok(true);
    }

    private Long parseTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return 1L;
        }
        return Long.parseLong(tenantIdHeader);
    }
}
