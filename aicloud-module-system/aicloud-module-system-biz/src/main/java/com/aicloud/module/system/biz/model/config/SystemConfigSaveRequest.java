package com.aicloud.module.system.biz.model.config;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class SystemConfigSaveRequest {
    private Long id;
    private String configKey;
    private String configName;
    private String configValue;
    private Integer status;
    private String remark;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }
    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }
    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
