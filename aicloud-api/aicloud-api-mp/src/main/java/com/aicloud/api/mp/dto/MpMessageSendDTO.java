package com.aicloud.api.mp.dto;

import java.util.Map;

/**
 * Mini program message send DTO.
 *
 * @author yifei
 */
public class MpMessageSendDTO {

    private Long tenantId;
    private Long userId;
    private String openId;
    private String templateCode;
    private Map<String, String> params;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public Map<String, String> getParams() { return params; }
    public void setParams(Map<String, String> params) { this.params = params; }

    
    public String toString() {
        return "MpMessageSendDTO{}";
    }
}
