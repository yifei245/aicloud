package com.aicloud.gateway.service.export;

import com.aicloud.gateway.entity.AiAuditExportTask;
import com.aicloud.gateway.mapper.AuditExportTaskMapper;
import com.aicloud.gateway.model.AuditLogItemResponse;
import com.aicloud.common.pojo.PageResponse;
import com.aicloud.gateway.model.export.AuditExportCleanupResult;
import com.aicloud.gateway.model.export.AuditExportTaskInfo;
import com.aicloud.gateway.model.export.AuditExportTaskRequest;
import com.aicloud.gateway.model.export.AuditExportTaskStatus;
import com.aicloud.gateway.service.AuditArchiveService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class AuditExportTaskService {

    private final AuditArchiveService auditArchiveService;
    private final AuditExportTaskMapper auditExportTaskMapper;
    private final Executor auditTaskExecutor;
    private final Path exportDir = Path.of(System.getProperty("java.io.tmpdir"), "aicloud-audit-exports");

    public AuditExportTaskService(AuditArchiveService auditArchiveService, AuditExportTaskMapper auditExportTaskMapper,
                                  @Qualifier("auditTaskExecutor") Executor auditTaskExecutor) {
        this.auditArchiveService = auditArchiveService;
        this.auditExportTaskMapper = auditExportTaskMapper;
        this.auditTaskExecutor = auditTaskExecutor;
    }

    public AuditExportTaskInfo createTask(AuditExportTaskRequest request) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        AiAuditExportTask task = new AiAuditExportTask();
        task.setTaskId(taskId);
        task.setStatus(AuditExportTaskStatus.PENDING.name());
        task.setMessage("排队中");
        task.setArchived(request.isArchived() ? 1 : 0);
        task.setTenantId(request.getTenantId());
        task.setUserId(request.getUserId());
        task.setSuccess(request.getSuccess());
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setMaxRows(request.getMaxRows() == null ? 5000 : request.getMaxRows());
        task.setCreatedAt(LocalDateTime.now());
        auditExportTaskMapper.insert(task);

        auditTaskExecutor.execute(() -> runTask(taskId, request));
        return toInfo(task);
    }

    public AuditExportTaskInfo getTask(String taskId) {
        AiAuditExportTask task = queryByTaskId(taskId);
        return task == null ? null : toInfo(task);
    }

    public PageResponse<AuditExportTaskInfo> queryTasks(AuditExportTaskStatus status,
                                                        Boolean archived,
                                                        Long tenantId,
                                                        Long userId,
                                                        Integer success,
                                                        String keyword,
                                                        LocalDateTime startTime,
                                                        LocalDateTime endTime,
                                                        String sortBy,
                                                        String sortOrder,
                                                        long pageNo,
                                                        long pageSize) {
        long safePageNo = Math.max(pageNo, 1);
        long safePageSize = Math.min(Math.max(pageSize, 1), 200);
        boolean asc = "asc".equalsIgnoreCase(sortOrder);

        Page<AiAuditExportTask> page = new Page<>(safePageNo, safePageSize);
        LambdaQueryWrapper<AiAuditExportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, AiAuditExportTask::getStatus, status.name())
                .eq(archived != null, AiAuditExportTask::getArchived, Boolean.TRUE.equals(archived) ? 1 : 0)
                .eq(tenantId != null, AiAuditExportTask::getTenantId, tenantId)
                .eq(userId != null, AiAuditExportTask::getUserId, userId)
                .eq(success != null, AiAuditExportTask::getSuccess, success)
                .and(StringUtils.hasText(keyword), w -> w.like(AiAuditExportTask::getTaskId, keyword)
                        .or()
                        .like(AiAuditExportTask::getFilename, keyword))
                .ge(startTime != null, AiAuditExportTask::getCreatedAt, startTime)
                .le(endTime != null, AiAuditExportTask::getCreatedAt, endTime);
        applyOrder(wrapper, sortBy, asc);
        Page<AiAuditExportTask> result = auditExportTaskMapper.selectPage(page, wrapper);

        PageResponse<AuditExportTaskInfo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageNo(result.getCurrent());
        response.setPageSize(result.getSize());
        response.setList(result.getRecords().stream().map(this::toInfo).toList());
        return response;
    }

    private void applyOrder(LambdaQueryWrapper<AiAuditExportTask> wrapper, String sortBy, boolean asc) {
        if ("finishedAt".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, AiAuditExportTask::getFinishedAt)
                    .orderByDesc(AiAuditExportTask::getId);
            return;
        }
        if ("id".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, AiAuditExportTask::getId);
            return;
        }
        wrapper.orderBy(true, asc, AiAuditExportTask::getCreatedAt)
                .orderByDesc(AiAuditExportTask::getId);
    }

    public byte[] readFile(String taskId) throws IOException {
        AiAuditExportTask task = queryByTaskId(taskId);
        if (task == null || !AuditExportTaskStatus.SUCCESS.name().equals(task.getStatus()) || task.getFilename() == null) {
            return null;
        }
        Path path = exportDir.resolve(task.getFilename());
        if (!Files.exists(path)) {
            return null;
        }
        return Files.readAllBytes(path);
    }

    public AuditExportCleanupResult cleanupFinishedTasks(int retentionDays, int batchSize) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(Math.max(retentionDays, 1));
        int safeBatch = Math.max(batchSize, 1);

        LambdaQueryWrapper<AiAuditExportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(AiAuditExportTask::getStatus, AuditExportTaskStatus.SUCCESS.name(), AuditExportTaskStatus.FAILED.name())
                .lt(AiAuditExportTask::getFinishedAt, cutoff)
                .orderByAsc(AiAuditExportTask::getId)
                .last("LIMIT " + safeBatch);
        List<AiAuditExportTask> tasks = auditExportTaskMapper.selectList(wrapper);

        int deletedFiles = 0;
        for (AiAuditExportTask task : tasks) {
            if (task.getFilename() == null || task.getFilename().isBlank()) {
                continue;
            }
            Path file = exportDir.resolve(task.getFilename());
            try {
                if (Files.deleteIfExists(file)) {
                    deletedFiles++;
                }
            } catch (IOException ignored) {
                // Ignore file deletion errors, continue with DB cleanup.
            }
        }

        int deletedRows = 0;
        if (!tasks.isEmpty()) {
            List<Long> ids = tasks.stream().map(AiAuditExportTask::getId).collect(Collectors.toList());
            deletedRows = auditExportTaskMapper.deleteBatchIds(ids);
        }

        AuditExportCleanupResult result = new AuditExportCleanupResult();
        result.setScanned(tasks.size());
        result.setDeletedRows(deletedRows);
        result.setDeletedFiles(deletedFiles);
        return result;
    }

    private void runTask(String taskId, AuditExportTaskRequest request) {
        updateStatus(taskId, AuditExportTaskStatus.RUNNING, "处理中", null, null, null);
        try {
            Files.createDirectories(exportDir);
            int maxRows = request.getMaxRows() == null ? 5000 : request.getMaxRows();
            List<AuditLogItemResponse> logs = auditArchiveService.queryLogsForExport(
                    request.isArchived(),
                    request.getTenantId(),
                    request.getUserId(),
                    request.getSuccess(),
                    request.getStartTime(),
                    request.getEndTime(),
                    maxRows);
            byte[] csv = auditArchiveService.buildCsv(logs);

            String filename = "gateway-audit-export-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + taskId + ".csv";
            Path file = exportDir.resolve(filename);
            Files.write(file, csv);

            updateStatus(taskId, AuditExportTaskStatus.SUCCESS, "完成", filename, (long) csv.length, LocalDateTime.now());
        } catch (Exception ex) {
            updateStatus(taskId, AuditExportTaskStatus.FAILED, "导出失败，请稍后重试", null, null, LocalDateTime.now());
        }
    }

    private void updateStatus(String taskId, AuditExportTaskStatus status, String message,
                              String filename, Long fileSize, LocalDateTime finishedAt) {
        LambdaUpdateWrapper<AiAuditExportTask> update = new LambdaUpdateWrapper<>();
        update.eq(AiAuditExportTask::getTaskId, taskId)
                .set(AiAuditExportTask::getStatus, status.name())
                .set(AiAuditExportTask::getMessage, message)
                .set(filename != null, AiAuditExportTask::getFilename, filename)
                .set(fileSize != null, AiAuditExportTask::getFileSize, fileSize)
                .set(finishedAt != null, AiAuditExportTask::getFinishedAt, finishedAt);
        auditExportTaskMapper.update(null, update);
    }

    private AiAuditExportTask queryByTaskId(String taskId) {
        return auditExportTaskMapper.selectOne(
                new LambdaQueryWrapper<AiAuditExportTask>().eq(AiAuditExportTask::getTaskId, taskId).last("LIMIT 1"));
    }

    private AuditExportTaskInfo toInfo(AiAuditExportTask task) {
        AuditExportTaskInfo info = new AuditExportTaskInfo();
        info.setTaskId(task.getTaskId());
        info.setStatus(AuditExportTaskStatus.valueOf(task.getStatus()));
        info.setMessage(task.getMessage());
        info.setArchived(task.getArchived());
        info.setTenantId(task.getTenantId());
        info.setUserId(task.getUserId());
        info.setSuccess(task.getSuccess());
        info.setMaxRows(task.getMaxRows());
        info.setStartTime(task.getStartTime());
        info.setEndTime(task.getEndTime());
        info.setFilename(task.getFilename());
        info.setFileSize(task.getFileSize());
        info.setCreatedAt(task.getCreatedAt());
        info.setFinishedAt(task.getFinishedAt());
        return info;
    }
}
