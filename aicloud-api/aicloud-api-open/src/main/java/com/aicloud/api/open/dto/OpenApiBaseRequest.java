package com.aicloud.api.open.dto;

/**
 * OpenAPI signed request base fields.
 *
 * @author yifei
 */
public class OpenApiBaseRequest {

    private Long tenantId;
    private String appKey;
    private String requestId;
    private Long timestamp;
    private String nonce;
    private String sign;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getAppKey() { return appKey; }
    public void setAppKey(String appKey) { this.appKey = appKey; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }
    public String getSign() { return sign; }
    public void setSign(String sign) { this.sign = sign; }

    
    public String toString() {
        return "OpenApiBaseRequest{}";
    }
}
