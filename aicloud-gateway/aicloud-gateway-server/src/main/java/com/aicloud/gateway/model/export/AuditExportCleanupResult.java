package com.aicloud.gateway.model.export;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class AuditExportCleanupResult {

    private int scanned;
    private int deletedRows;
    private int deletedFiles;

    public int getScanned() { return scanned; }
    public void setScanned(int scanned) { this.scanned = scanned; }
    public int getDeletedRows() { return deletedRows; }
    public void setDeletedRows(int deletedRows) { this.deletedRows = deletedRows; }
    public int getDeletedFiles() { return deletedFiles; }
    public void setDeletedFiles(int deletedFiles) { this.deletedFiles = deletedFiles; }
}
