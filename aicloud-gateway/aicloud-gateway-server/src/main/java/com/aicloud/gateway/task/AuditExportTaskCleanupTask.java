package com.aicloud.gateway.task;

import com.aicloud.gateway.model.export.AuditExportCleanupResult;
import com.aicloud.gateway.service.export.AuditExportTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class AuditExportTaskCleanupTask {

    private static final Logger LOG = LoggerFactory.getLogger(AuditExportTaskCleanupTask.class);

    private final AuditExportTaskService auditExportTaskService;

    @Value("${aicloud.gateway.audit.export.cleanup.enabled:true}")
    private boolean enabled;

    @Value("${aicloud.gateway.audit.export.cleanup.retention-days:7}")
    private int retentionDays;

    @Value("${aicloud.gateway.audit.export.cleanup.batch-size:500}")
    private int batchSize;

    public AuditExportTaskCleanupTask(AuditExportTaskService auditExportTaskService) {
        this.auditExportTaskService = auditExportTaskService;
    }

    @Scheduled(cron = "${aicloud.gateway.audit.export.cleanup.cron:0 40 3 * * ?}")
    public void cleanup() {
        if (!enabled) {
            return;
        }
        int totalRows = 0;
        int totalFiles = 0;
        while (true) {
            AuditExportCleanupResult result = auditExportTaskService.cleanupFinishedTasks(retentionDays, batchSize);
            if (result.getScanned() <= 0 || result.getDeletedRows() <= 0) {
                break;
            }
            totalRows += result.getDeletedRows();
            totalFiles += result.getDeletedFiles();
            if (result.getScanned() < batchSize) {
                break;
            }
        }
        if (totalRows > 0 || totalFiles > 0) {
            LOG.info("gateway_audit_export_cleanup finished deletedRows={} deletedFiles={}", totalRows, totalFiles);
        }
    }
}
