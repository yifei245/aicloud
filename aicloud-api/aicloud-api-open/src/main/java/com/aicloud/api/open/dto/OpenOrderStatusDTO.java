package com.aicloud.api.open.dto;

/**
 * OpenAPI order status DTO.
 *
 * @author yifei
 */
public class OpenOrderStatusDTO extends OpenApiBaseRequest {

    private String orderNo;
    private String status;
    private String logisticsNo;
    private String logisticsCompany;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLogisticsNo() { return logisticsNo; }
    public void setLogisticsNo(String logisticsNo) { this.logisticsNo = logisticsNo; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }

    
    public String toString() {
        return "OpenOrderStatusDTO{}";
    }
}
