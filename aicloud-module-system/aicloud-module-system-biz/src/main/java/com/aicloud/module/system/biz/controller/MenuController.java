package com.aicloud.module.system.biz.controller;

import com.aicloud.module.system.biz.annotation.RequirePermission;
import com.aicloud.module.system.biz.model.ApiResponse;
import com.aicloud.module.system.biz.model.MenuNode;
import com.aicloud.module.system.biz.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统权限")
@RestController
@RequestMapping("/system")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @Operation(summary = "获取动态菜单树")
    @RequirePermission("system:menu:list")
    @GetMapping("/menu/tree")
    public ApiResponse<List<MenuNode>> menuTree(@RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
                                                @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader) {
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return ApiResponse.fail(4001, "缺少或非法的 X-User-Id");
        }
        Long tenantId = parseTenantId(tenantIdHeader);
        return ApiResponse.ok(menuService.getMenuTree(tenantId, userId));
    }

    @Operation(summary = "获取全部菜单树")
    @RequirePermission("system:menu:list")
    @GetMapping("/menu/list")
    public ApiResponse<List<MenuNode>> menuList(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader) {
        Long tenantId = parseTenantId(tenantIdHeader);
        return ApiResponse.ok(menuService.getAllMenus(tenantId));
    }

    @Operation(summary = "获取按钮级权限")
    @RequirePermission("system:menu:list")
    @GetMapping("/permission/buttons")
    public ApiResponse<List<String>> buttonPermissions(@RequestHeader(name = "X-User-Id", required = false) String userIdHeader,
                                                       @RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader) {
        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            return ApiResponse.fail(4001, "缺少或非法的 X-User-Id");
        }
        Long tenantId = parseTenantId(tenantIdHeader);
        return ApiResponse.ok(menuService.getButtonPermissions(tenantId, userId));
    }

    private Long parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(userIdHeader);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long parseTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return 1L;
        }
        try {
            return Long.parseLong(tenantIdHeader);
        } catch (NumberFormatException ex) {
            return 1L;
        }
    }
}
