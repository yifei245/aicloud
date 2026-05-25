package com.aicloud.gateway.model;

import java.time.LocalDateTime;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class AuditArchiveRunResponse {

    private LocalDateTime cutoffTime;
    private long totalBefore;
    private int archived;
    private int deleted;

    public LocalDateTime getCutoffTime() {
        return cutoffTime;
    }

    public void setCutoffTime(LocalDateTime cutoffTime) {
        this.cutoffTime = cutoffTime;
    }

    public long getTotalBefore() {
        return totalBefore;
    }

    public void setTotalBefore(long totalBefore) {
        this.totalBefore = totalBefore;
    }

    public int getArchived() {
        return archived;
    }

    public void setArchived(int archived) {
        this.archived = archived;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }
}
