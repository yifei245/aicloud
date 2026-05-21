package com.aicloud.gateway.model;

import java.time.LocalDateTime;

public class AuditArchiveStatsResponse {

    private LocalDateTime cutoffTime;
    private long currentLogCount;
    private long archiveLogCount;
    private long pendingArchiveCount;
    private LocalDateTime latestArchivedTime;

    public LocalDateTime getCutoffTime() {
        return cutoffTime;
    }

    public void setCutoffTime(LocalDateTime cutoffTime) {
        this.cutoffTime = cutoffTime;
    }

    public long getCurrentLogCount() {
        return currentLogCount;
    }

    public void setCurrentLogCount(long currentLogCount) {
        this.currentLogCount = currentLogCount;
    }

    public long getArchiveLogCount() {
        return archiveLogCount;
    }

    public void setArchiveLogCount(long archiveLogCount) {
        this.archiveLogCount = archiveLogCount;
    }

    public long getPendingArchiveCount() {
        return pendingArchiveCount;
    }

    public void setPendingArchiveCount(long pendingArchiveCount) {
        this.pendingArchiveCount = pendingArchiveCount;
    }

    public LocalDateTime getLatestArchivedTime() {
        return latestArchivedTime;
    }

    public void setLatestArchivedTime(LocalDateTime latestArchivedTime) {
        this.latestArchivedTime = latestArchivedTime;
    }
}
