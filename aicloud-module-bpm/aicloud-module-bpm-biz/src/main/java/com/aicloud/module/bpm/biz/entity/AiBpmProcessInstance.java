package com.aicloud.module.bpm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ai_bpm_process_instance")
/**
 * BPM process instance mirror for Flowable runtime instances.
 *
 * @author yifei
 */
public class AiBpmProcessInstance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String instanceNo;
    private Long processDefinitionId;
    private String processKey;
    private String businessId;
    private String starter;
    private String currentAssignee;
    private String status;
    private String engineInstanceId;
    private String engineDefinitionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getInstanceNo() { return instanceNo; }
    public void setInstanceNo(String instanceNo) { this.instanceNo = instanceNo; }
    public Long getProcessDefinitionId() { return processDefinitionId; }
    public void setProcessDefinitionId(Long processDefinitionId) { this.processDefinitionId = processDefinitionId; }
    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getStarter() { return starter; }
    public void setStarter(String starter) { this.starter = starter; }
    public String getCurrentAssignee() { return currentAssignee; }
    public void setCurrentAssignee(String currentAssignee) { this.currentAssignee = currentAssignee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEngineInstanceId() { return engineInstanceId; }
    public void setEngineInstanceId(String engineInstanceId) { this.engineInstanceId = engineInstanceId; }
    public String getEngineDefinitionId() { return engineDefinitionId; }
    public void setEngineDefinitionId(String engineDefinitionId) { this.engineDefinitionId = engineDefinitionId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
