package com.aicloud.gateway.task;

import com.aicloud.gateway.model.AuditArchiveRunResponse;
import com.aicloud.gateway.service.AuditArchiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class AuditLogArchiveTask {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogArchiveTask.class);

    private final AuditArchiveService auditArchiveService;

    @Value("${aicloud.gateway.audit.archive.enabled:true}")
    private boolean enabled;

    @Value("${aicloud.gateway.audit.archive.retention-days:30}")
    private int retentionDays;

    @Value("${aicloud.gateway.audit.archive.batch-size:1000}")
    private int batchSize;

    public AuditLogArchiveTask(AuditArchiveService auditArchiveService) {
        this.auditArchiveService = auditArchiveService;
    }

    @Scheduled(cron = "${aicloud.gateway.audit.archive.cron:0 15 3 * * ?}")
    public void archiveAndCleanup() {
        if (!enabled) {
            return;
        }
        AuditArchiveRunResponse result = auditArchiveService.runOnce(retentionDays, batchSize);
        if (result.getTotalBefore() <= 0) {
            return;
        }
        LOG.info("gateway_audit_archive finished cutoff={} totalBefore={} archived={} deleted={}",
                result.getCutoffTime(), result.getTotalBefore(), result.getArchived(), result.getDeleted());
    }
}
