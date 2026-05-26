package com.aicloud.gateway.service.export;

import com.aicloud.common.pojo.PageResponse;
import com.aicloud.gateway.model.AuditLogItemResponse;
import com.aicloud.gateway.model.export.AuditExportCleanupResult;
import com.aicloud.gateway.model.export.AuditExportTaskInfo;
import com.aicloud.gateway.model.export.AuditExportTaskRequest;
import com.aicloud.gateway.model.export.AuditExportTaskStatus;
import com.aicloud.gateway.service.AuditArchiveService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Executor auditTaskExecutor;
    private final Map<String, AuditExportTaskInfo> tasks = new ConcurrentHashMap<>();
    private final Path exportDir = Path.of(System.getProperty("java.io.tmpdir"), "aicloud-audit-exports");

    public AuditExportTaskService(AuditArchiveService auditArchiveService,
                                  @Qualifier("auditTaskExecutor") Executor auditTaskExecutor) {
        this.auditArchiveService = auditArchiveService;
        this.auditTaskExecutor = auditTaskExecutor;
    }

    public AuditExportTaskInfo createTask(AuditExportTaskRequest request) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        AuditExportTaskInfo task = new AuditExportTaskInfo();
        task.setTaskId(taskId);
        task.setStatus(AuditExportTaskStatus.PENDING);
        task.setMessage("排队中，gateway 不再持久化任务状态");
        task.setArchived(request.isArchived() ? 1 : 0);
        task.setTenantId(request.getTenantId());
        task.setUserId(request.getUserId());
        task.setSuccess(request.getSuccess());
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setMaxRows(request.getMaxRows() == null ? 5000 : request.getMaxRows());
        task.setCreatedAt(LocalDateTime.now());
        tasks.put(taskId, task);

        auditTaskExecutor.execute(() -> runTask(taskId, request));
        return copy(task);
    }

    public AuditExportTaskInfo getTask(String taskId) {
        AuditExportTaskInfo task = tasks.get(taskId);
        return task == null ? null : copy(task);
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
        Comparator<AuditExportTaskInfo> comparator = comparator(sortBy);
        if (!"asc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        List<AuditExportTaskInfo> filtered = tasks.values().stream()
                .filter(item -> status == null || status == item.getStatus())
                .filter(item -> archived == null || (Boolean.TRUE.equals(archived) ? 1 : 0) == nullToZero(item.getArchived()))
                .filter(item -> tenantId == null || tenantId.equals(item.getTenantId()))
                .filter(item -> userId == null || userId.equals(item.getUserId()))
                .filter(item -> success == null || success.equals(item.getSuccess()))
                .filter(item -> !StringUtils.hasText(keyword)
                        || contains(item.getTaskId(), keyword) || contains(item.getFilename(), keyword))
                .filter(item -> startTime == null || !item.getCreatedAt().isBefore(startTime))
                .filter(item -> endTime == null || !item.getCreatedAt().isAfter(endTime))
                .sorted(comparator)
                .map(this::copy)
                .toList();
        int fromIndex = (int) Math.min((safePageNo - 1) * safePageSize, filtered.size());
        int toIndex = (int) Math.min(fromIndex + safePageSize, filtered.size());

        PageResponse<AuditExportTaskInfo> response = new PageResponse<>();
        response.setTotal(filtered.size());
        response.setPageNo(safePageNo);
        response.setPageSize(safePageSize);
        response.setList(filtered.subList(fromIndex, toIndex));
        return response;
    }

    public byte[] readFile(String taskId) throws IOException {
        AuditExportTaskInfo task = tasks.get(taskId);
        if (task == null || task.getStatus() != AuditExportTaskStatus.SUCCESS || task.getFilename() == null) {
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
        List<AuditExportTaskInfo> expired = tasks.values().stream()
                .filter(item -> item.getFinishedAt() != null && item.getFinishedAt().isBefore(cutoff))
                .limit(safeBatch)
                .toList();

        int deletedFiles = 0;
        int deletedRows = 0;
        for (AuditExportTaskInfo task : expired) {
            if (StringUtils.hasText(task.getFilename())) {
                try {
                    if (Files.deleteIfExists(exportDir.resolve(task.getFilename()))) {
                        deletedFiles++;
                    }
                } catch (IOException ignored) {
                    // Ignore file deletion errors; gateway cleanup must not affect request processing.
                }
            }
            if (tasks.remove(task.getTaskId()) != null) {
                deletedRows++;
            }
        }

        AuditExportCleanupResult result = new AuditExportCleanupResult();
        result.setScanned(expired.size());
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

            String filename = "gateway-audit-export-"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "-" + taskId + ".csv";
            Path file = exportDir.resolve(filename);
            Files.write(file, csv);
            updateStatus(taskId, AuditExportTaskStatus.SUCCESS, "完成", filename, (long) csv.length, LocalDateTime.now());
        } catch (Exception ex) {
            updateStatus(taskId, AuditExportTaskStatus.FAILED, "导出失败，请稍后重试", null, null, LocalDateTime.now());
        }
    }

    private void updateStatus(String taskId, AuditExportTaskStatus status, String message,
                              String filename, Long fileSize, LocalDateTime finishedAt) {
        AuditExportTaskInfo task = tasks.get(taskId);
        if (task == null) {
            return;
        }
        task.setStatus(status);
        task.setMessage(message);
        if (filename != null) {
            task.setFilename(filename);
        }
        if (fileSize != null) {
            task.setFileSize(fileSize);
        }
        if (finishedAt != null) {
            task.setFinishedAt(finishedAt);
        }
    }

    private Comparator<AuditExportTaskInfo> comparator(String sortBy) {
        if ("finishedAt".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(AuditExportTaskInfo::getFinishedAt, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        return Comparator.comparing(AuditExportTaskInfo::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.contains(keyword);
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private AuditExportTaskInfo copy(AuditExportTaskInfo source) {
        AuditExportTaskInfo target = new AuditExportTaskInfo();
        target.setTaskId(source.getTaskId());
        target.setStatus(source.getStatus());
        target.setMessage(source.getMessage());
        target.setArchived(source.getArchived());
        target.setTenantId(source.getTenantId());
        target.setUserId(source.getUserId());
        target.setSuccess(source.getSuccess());
        target.setMaxRows(source.getMaxRows());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());
        target.setFilename(source.getFilename());
        target.setFileSize(source.getFileSize());
        target.setCreatedAt(source.getCreatedAt());
        target.setFinishedAt(source.getFinishedAt());
        return target;
    }
}
