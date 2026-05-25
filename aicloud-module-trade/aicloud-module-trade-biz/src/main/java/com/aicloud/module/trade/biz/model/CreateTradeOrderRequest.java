package com.aicloud.module.trade.biz.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class CreateTradeOrderRequest {
    @NotNull
    private Long tenantId;
    @NotBlank
    private String orderNo;
    @NotNull
    private Long userId;
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal totalAmount;
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal payAmount;
    @NotBlank
    private String status;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
