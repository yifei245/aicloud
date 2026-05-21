package com.aicloud.module.system.biz.controller;

import com.aicloud.module.system.biz.annotation.RequirePermission;
import com.aicloud.module.system.biz.model.ApiResponse;
import com.aicloud.module.system.biz.model.common.PageResult;
import com.aicloud.module.system.biz.model.user.UserPasswordRequest;
import com.aicloud.module.system.biz.model.user.UserResponse;
import com.aicloud.module.system.biz.model.user.UserSaveRequest;
import com.aicloud.module.system.biz.model.user.UserStatusRequest;
import com.aicloud.module.system.biz.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "系统用户")
@RestController
@RequestMapping("/system/user")
public class SystemUserController {

    private final UserAdminService userAdminService;

    public SystemUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @Operation(summary = "用户列表")
    @RequirePermission("system:user:query")
    @GetMapping("/list")
    public ApiResponse<PageResult<UserResponse>> list(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                                      @RequestParam(name = "keyword", required = false) String keyword,
                                                      @RequestParam(name = "status", required = false) Integer status,
                                                      @RequestParam(name = "deptId", required = false) Long deptId) {
        return ApiResponse.ok(userAdminService.list(parseTenantId(tenantIdHeader), keyword, status, deptId));
    }

    @Operation(summary = "用户详情")
    @RequirePermission("system:user:query")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                         @PathVariable("id") Long id) {
        return ApiResponse.ok(userAdminService.get(parseTenantId(tenantIdHeader), id));
    }

    @Operation(summary = "创建用户")
    @RequirePermission("system:user:create")
    @PostMapping("/create")
    public ApiResponse<UserResponse> create(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                            @RequestBody UserSaveRequest request) {
        return ApiResponse.ok(userAdminService.create(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "更新用户")
    @RequirePermission("system:user:update")
    @PutMapping("/update")
    public ApiResponse<UserResponse> update(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                            @RequestBody UserSaveRequest request) {
        return ApiResponse.ok(userAdminService.update(parseTenantId(tenantIdHeader), request));
    }

    @Operation(summary = "修改用户状态")
    @RequirePermission("system:user:update")
    @PutMapping("/status")
    public ApiResponse<Boolean> updateStatus(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                             @RequestBody UserStatusRequest request) {
        userAdminService.updateStatus(parseTenantId(tenantIdHeader), request.getId(), request.getStatus());
        return ApiResponse.ok(true);
    }

    @Operation(summary = "重置用户密码")
    @RequirePermission("system:user:reset-password")
    @PutMapping("/reset-password")
    public ApiResponse<Boolean> resetPassword(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                              @RequestBody UserPasswordRequest request) {
        userAdminService.resetPassword(parseTenantId(tenantIdHeader), request.getId(), request.getPassword());
        return ApiResponse.ok(true);
    }

    @Operation(summary = "删除用户")
    @RequirePermission("system:user:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                       @PathVariable("id") Long id) {
        userAdminService.delete(parseTenantId(tenantIdHeader), id);
        return ApiResponse.ok(true);
    }

    private Long parseTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return 1L;
        }
        return Long.parseLong(tenantIdHeader);
    }
}
