package com.aicloud.gateway.model;

import java.time.LocalDateTime;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class AuditLogItemResponse {

    private Long id;
    private Long sourceId;
    private Long tenantId;
    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String requestUri;
    private String requestMethod;
    private String requestIp;
    private Integer success;
    private String errorMsg;
    private LocalDateTime createTime;
    private LocalDateTime archivedTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }
    public Integer getSuccess() { return success; }
    public void setSuccess(Integer success) { this.success = success; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getArchivedTime() { return archivedTime; }
    public void setArchivedTime(LocalDateTime archivedTime) { this.archivedTime = archivedTime; }
}
