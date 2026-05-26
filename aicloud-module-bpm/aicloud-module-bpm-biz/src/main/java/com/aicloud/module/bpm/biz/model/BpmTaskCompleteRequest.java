package com.aicloud.module.bpm.biz.model;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Flowable task complete request.
 *
 * @author yifei
 */
public class BpmTaskCompleteRequest {

    @NotNull
    private Long taskId;
    private Map<String, Object> variables;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}
