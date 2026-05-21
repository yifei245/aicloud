package com.aicloud.gateway.controller;

import com.aicloud.gateway.model.ApiResponse;
import com.aicloud.gateway.model.AuditArchiveRunResponse;
import com.aicloud.gateway.model.AuditArchiveStatsResponse;
import com.aicloud.gateway.model.AuditLogItemResponse;
import com.aicloud.gateway.model.PageResponse;
import com.aicloud.gateway.model.export.AuditExportTaskStatus;
import com.aicloud.gateway.model.export.AuditExportTaskInfo;
import com.aicloud.gateway.model.export.AuditExportTaskRequest;
import com.aicloud.gateway.model.export.AuditExportCleanupResult;
import com.aicloud.gateway.service.AuditArchiveService;
import com.aicloud.gateway.service.export.AuditExportTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "网关审计管理")
@RestController
@RequestMapping("/gateway/audit")
public class AuditAdminController {

    private final AuditArchiveService auditArchiveService;
    private final AuditExportTaskService auditExportTaskService;

    public AuditAdminController(AuditArchiveService auditArchiveService, AuditExportTaskService auditExportTaskService) {
        this.auditArchiveService = auditArchiveService;
        this.auditExportTaskService = auditExportTaskService;
    }

    @GetMapping("/stats")
    @Operation(summary = "审计归档统计", description = "需要管理员角色。建议请求头同时携带 Authorization 和 X-Tenant-Id。")
    public ApiResponse<AuditArchiveStatsResponse> stats(
            @Parameter(description = "由网关透传的角色头，包含 ADMIN 或 SUPER_ADMIN 时可访问")
            @RequestHeader(name = "X-User-Roles", required = false) String roles,
            @RequestParam(name = "retentionDays", defaultValue = "30") int retentionDays) {
        if (!isAdmin(roles)) {
            return ApiResponse.fail(403, "需要管理员角色");
        }
        return ApiResponse.ok(auditArchiveService.stats(retentionDays));
    }

    @PostMapping("/archive/run")
    @Operation(summary = "立即执行审计归档", description = "需要管理员角色。")
    public ApiResponse<AuditArchiveRunResponse> runArchive(
            @RequestHeader(name = "X-User-Roles", required = false) String roles,
            @RequestParam(name = "retentionDays", defaultValue = "30") int retentionDays,
            @RequestParam(name = "batchSize", defaultValue = "1000") int batchSize) {
        if (!isAdmin(roles)) {
            return ApiResponse.fail(403, "需要管理员角色");
        }
        return ApiResponse.ok(auditArchiveService.runOnce(retentionDays, batchSize));
    }

    @GetMapping("/logs")
    @Operation(summary = "分页查询审计日志", description = "需要管理员角色。用于后台审计检索。")
    public ApiResponse<PageResponse<AuditLogItemResponse>> logs(
            @RequestHeader(name = "X-User-Roles", required = false) String roles,
            @RequestParam(name = "archived", defaultValue = "false") boolean archived,
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "success", required = false) Integer success,
            @RequestParam(name = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") long pageSize) {
        if (!isAdmin(roles)) {
            return ApiResponse.fail(403, "需要管理员角色");
        }
        return ApiResponse.ok(auditArchiveService.queryLogs(
                archived, tenantId, userId, success, startTime, endTime, pageNo, pageSize));
    }

    @GetMapping("/logs/export")
    @Operation(summary = "直接导出审计日志 CSV", description = "需要管理员角色。返回 CSV 文件流。")
    public ResponseEntity<byte[]> exportLogs(
            @RequestHeader(name = "X-User-Roles", required = false) String roles,
            @RequestParam(name = "archived", defaultValue = "false") boolean archived,
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "success", required = false) Integer success,
            @RequestParam(name = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(name = "maxRows", defaultValue = "5000") int maxRows) {
        if (!isAdmin(roles)) {
            byte[] body = "{\"code\":403,\"msg\":\"需要管理员角色\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.status(403)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        }
        List<AuditLogItemResponse> logs = auditArchiveService.queryLogsForExport(
                archived, tenantId, userId, success, startTime, endTime, maxRows);
        byte[] csv = auditArchiveService.buildCsv(logs);
        String filename = "gateway-audit-logs-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(csv);
    }

    @PostMapping("/export/tasks")
    @Operation(summary = "创建异步导出任务", description = "需要管理员角色。适合大数据量导出。")
    public ApiResponse<AuditExportTaskInfo> createExportTask(
            @RequestHeader(name = "X-User-Roles", required = false) String roles,
            @RequestParam(name = "archived", defaultValue = "false") boolean archived,
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "success", required = false) Integer success,
            @RequestParam(name = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(name = "maxRows", defaultValue = "5000") int maxRows) {
        if (!isAdmin(roles)) {
            return ApiResponse.fail(403, "需要管理员角色");
        }
        AuditExportTaskRequest request = new AuditExportTaskRequest();
        request.setArchived(archived);
        request.setTenantId(tenantId);
        request.setUserId(userId);
        request.setSuccess(success);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setMaxRows(maxRows);
        return ApiResponse.ok(auditExportTaskService.createTask(request));
    }

    @GetMapping("/export/tasks/{taskId}")
    @Operation(summary = "查询导出任务详情", description = "需要管理员角色。")
    public ApiResponse<AuditExportTaskInfo> getExportTask(
            @RequestHeader(name = "X-User-Roles", required = false) String roles,
            @PathVariable("taskId") String taskId) {
        if (!isAdmin(roles)) {
            return ApiResponse.fail(403, "需要管理员角色");
        }
        AuditExportTaskInfo info = auditExportTaskService.getTask(taskId);
        if (info == null) {
            return ApiResponse.fail(404, "任务不存在");
        }
        return ApiResponse.ok(info);
    }

    @GetMapping("/export/tasks/list")
    @Operation(summary = "分页查询导出任务", description = "需要管理员角色。")
    public ApiResponse<PageResponse<AuditExportTaskInfo>> listExportTasks(
            @RequestHeader(name = "X-User-Roles", required = false) String roles,
            @RequestParam(name = "status", required = false) AuditExportTaskStatus status,
            @RequestParam(name = "archived", required = false) Boolean archived,
            @RequestParam(name = "tenantId", required = false) Long tenantId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "success", required = false) Integer success,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "startTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(name = "endTime", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "desc") String sortOrder,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") long pageSize) {
        if (!isAdmin(roles)) {
            return ApiResponse.fail(403, "需要管理员角色");
        }
        return ApiResponse.ok(auditExportTaskService.queryTasks(
                status, archived, tenantId, userId, success, keyword, startTime, endTime, sortBy, sortOrder, pageNo, pageSize));
    }

    @GetMapping("/export/tasks/{taskId}/download")
    @Operation(summary = "下载导出结果文件", description = "需要管理员角色。仅任务成功后可下载。")
    public ResponseEntity<byte[]> downloadExportTask(
            @RequestHeader(name = "X-User-Roles", required = false) String roles,
            @PathVariable("taskId") String taskId) {
        if (!isAdmin(roles)) {
            byte[] body = "{\"code\":403,\"msg\":\"需要管理员角色\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.status(403).contentType(MediaType.APPLICATION_JSON).body(body);
        }
        AuditExportTaskInfo info = auditExportTaskService.getTask(taskId);
        if (info == null) {
            byte[] body = "{\"code\":404,\"msg\":\"任务不存在\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON).body(body);
        }
        if (info.getStatus() != null && !"SUCCESS".equals(info.getStatus().name())) {
            byte[] body = ("{\"code\":409,\"msg\":\"任务状态: " + info.getStatus().name() + "\",\"data\":null}")
                    .getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.status(409).contentType(MediaType.APPLICATION_JSON).body(body);
        }
        try {
            byte[] file = auditExportTaskService.readFile(taskId);
            if (file == null) {
                byte[] body = "{\"code\":404,\"msg\":\"文件不存在\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
                return ResponseEntity.status(404).contentType(MediaType.APPLICATION_JSON).body(body);
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + info.getFilename())
                    .body(file);
        } catch (Exception ex) {
            byte[] body = ("{\"code\":500,\"msg\":\"下载失败: " + ex.getMessage() + "\",\"data\":null}")
                    .getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON).body(body);
        }
    }

    @PostMapping("/export/tasks/cleanup/run")
    @Operation(summary = "立即执行导出任务清理", description = "需要管理员角色。")
    public ApiResponse<AuditExportCleanupResult> runExportTaskCleanup(
            @RequestHeader(name = "X-User-Roles", required = false) String roles,
            @RequestParam(name = "retentionDays", defaultValue = "7") int retentionDays,
            @RequestParam(name = "batchSize", defaultValue = "500") int batchSize) {
        if (!isAdmin(roles)) {
            return ApiResponse.fail(403, "需要管理员角色");
        }
        int totalScanned = 0;
        int totalDeletedRows = 0;
        int totalDeletedFiles = 0;
        while (true) {
            AuditExportCleanupResult item = auditExportTaskService.cleanupFinishedTasks(retentionDays, batchSize);
            if (item.getScanned() <= 0 || item.getDeletedRows() <= 0) {
                break;
            }
            totalScanned += item.getScanned();
            totalDeletedRows += item.getDeletedRows();
            totalDeletedFiles += item.getDeletedFiles();
            if (item.getScanned() < Math.max(batchSize, 1)) {
                break;
            }
        }
        AuditExportCleanupResult result = new AuditExportCleanupResult();
        result.setScanned(totalScanned);
        result.setDeletedRows(totalDeletedRows);
        result.setDeletedFiles(totalDeletedFiles);
        return ApiResponse.ok(result);
    }

    private boolean isAdmin(String roles) {
        if (roles == null || roles.isBlank()) {
            return false;
        }
        return roles.contains("SUPER_ADMIN") || roles.contains("ADMIN");
    }
}
