package com.aicloud.module.system.biz.controller;

import com.aicloud.module.system.biz.annotation.RequirePermission;
import com.aicloud.module.system.biz.model.ApiResponse;
import com.aicloud.module.system.biz.model.dept.DeptResponse;
import com.aicloud.module.system.biz.model.dept.DeptSaveRequest;
import com.aicloud.module.system.biz.service.DeptAdminService;
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
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统部门")
@RestController
@RequestMapping("/system/dept")
public class DeptController {

    private final DeptAdminService deptAdminService;

    public DeptController(DeptAdminService deptAdminService) {
        this.deptAdminService = deptAdminService;
    }

    @Operation(summary = "部门树")
    @RequirePermission("system:dept:query")
    @GetMapping("/tree")
    public ApiResponse<List<DeptResponse>> tree(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader) {
        return ApiResponse.ok(deptAdminService.tree(parseTenantId(tenantIdHeader)));
    }

    @Operation(summary = "创建部门")
    @RequirePermission("system:dept:create")
    @PostMapping("/create")
    public ApiResponse<DeptResponse> create(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                            @RequestBody DeptSaveRequest request) {
        return ApiResponse.ok(deptAdminService.save(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "更新部门")
    @RequirePermission("system:dept:update")
    @PutMapping("/update")
    public ApiResponse<DeptResponse> update(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                            @RequestBody DeptSaveRequest request) {
        return ApiResponse.ok(deptAdminService.save(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "删除部门")
    @RequirePermission("system:dept:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                       @PathVariable("id") Long id) {
        deptAdminService.delete(parseTenantId(tenantIdHeader), id);
        return ApiResponse.ok(true);
    }

    private Long parseTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return 1L;
        }
        return Long.parseLong(tenantIdHeader);
    }
}
