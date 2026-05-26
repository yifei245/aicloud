package com.aicloud.module.bpm.biz.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Flowable process instance start request.
 *
 * @author yifei
 */
public class BpmInstanceStartRequest {

    @NotNull
    private Long tenantId;
    @NotBlank
    private String processKey;
    private String businessId;
    private String starter;
    private String currentAssignee;
    private Map<String, Object> variables;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getStarter() { return starter; }
    public void setStarter(String starter) { this.starter = starter; }
    public String getCurrentAssignee() { return currentAssignee; }
    public void setCurrentAssignee(String currentAssignee) { this.currentAssignee = currentAssignee; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}
