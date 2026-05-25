package com.aicloud.module.system.biz.controller;

import com.aicloud.module.system.biz.annotation.RequirePermission;
import com.aicloud.module.system.biz.entity.AiAuditLog;
import com.aicloud.module.system.biz.entity.AiLoginLog;
import com.aicloud.module.system.biz.model.ApiResponse;
import com.aicloud.module.system.biz.service.LogAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统日志")
@RestController
@RequestMapping("/system/log")
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class LogController {

    private final LogAdminService logAdminService;

    public LogController(LogAdminService logAdminService) {
        this.logAdminService = logAdminService;
    }

    @Operation(summary = "操作日志列表")
    @RequirePermission("system:log:query")
    @GetMapping("/operate/list")
    public ApiResponse<List<AiAuditLog>> listOperateLogs(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                                         @RequestParam(name = "module", required = false) String module,
                                                         @RequestParam(name = "username", required = false) String username,
                                                         @RequestParam(name = "success", required = false) Integer success) {
        return ApiResponse.ok(logAdminService.listAuditLogs(parseTenantId(tenantIdHeader), module, username, success));
    }

    @Operation(summary = "登录日志列表")
    @RequirePermission("system:log:query")
    @GetMapping("/login/list")
    public ApiResponse<List<AiLoginLog>> listLoginLogs(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantIdHeader,
                                                       @RequestParam(name = "username", required = false) String username,
                                                       @RequestParam(name = "terminal", required = false) String terminal,
                                                       @RequestParam(name = "success", required = false) Integer success) {
        return ApiResponse.ok(logAdminService.listLoginLogs(parseTenantId(tenantIdHeader), username, terminal, success));
    }

    private Long parseTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            return 1L;
        }
        return Long.parseLong(tenantIdHeader);
    }
}
