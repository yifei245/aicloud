package com.aicloud.module.bpm.biz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("ai_bpm_task")
/**
 * BPM task mirror for Flowable user tasks.
 *
 * @author yifei
 */
public class AiBpmTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String taskNo;
    private Long instanceId;
    private String taskName;
    private String assignee;
    private String status;
    private String engineTaskId;
    private String engineInstanceId;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public Long getInstanceId() { return instanceId; }
    public void setInstanceId(Long instanceId) { this.instanceId = instanceId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEngineTaskId() { return engineTaskId; }
    public void setEngineTaskId(String engineTaskId) { this.engineTaskId = engineTaskId; }
    public String getEngineInstanceId() { return engineInstanceId; }
    public void setEngineInstanceId(String engineInstanceId) { this.engineInstanceId = engineInstanceId; }
    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
