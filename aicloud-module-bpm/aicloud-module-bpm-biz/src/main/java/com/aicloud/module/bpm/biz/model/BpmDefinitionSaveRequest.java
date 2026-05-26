package com.aicloud.module.bpm.biz.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Flowable process definition save request.
 *
 * @author yifei
 */
public class BpmDefinitionSaveRequest {

    private Long id;
    @NotNull
    private Long tenantId;
    @NotBlank
    private String processKey;
    @NotBlank
    private String processName;
    @NotNull
    private Integer versionNo;
    private String status;
    private String category;
    private String createBy;
    private String assigneeExpression;
    private String bpmnXml;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public String getAssigneeExpression() { return assigneeExpression; }
    public void setAssigneeExpression(String assigneeExpression) { this.assigneeExpression = assigneeExpression; }
    public String getBpmnXml() { return bpmnXml; }
    public void setBpmnXml(String bpmnXml) { this.bpmnXml = bpmnXml; }
}
