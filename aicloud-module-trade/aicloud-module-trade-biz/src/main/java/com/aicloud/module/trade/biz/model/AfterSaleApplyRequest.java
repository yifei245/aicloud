package com.aicloud.module.trade.biz.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AfterSaleApplyRequest {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    @NotBlank(message = "售后类型不能为空")
    private String type;
    @NotBlank(message = "售后原因不能为空")
    private String reason;
    @NotNull(message = "售后金额不能为空")
    @DecimalMin(value = "0.01", message = "售后金额必须大于0")
    private BigDecimal amount;
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
