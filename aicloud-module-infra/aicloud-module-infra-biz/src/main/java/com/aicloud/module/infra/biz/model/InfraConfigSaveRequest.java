package com.aicloud.module.infra.biz.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InfraConfigSaveRequest {

    private Long id;

    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @NotBlank(message = "配置名称不能为空")
    private String configName;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    private String configType = "SYSTEM";
    private Integer status = 1;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public String getConfigType() { return configType; }
    public void setConfigType(String configType) { this.configType = configType; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
