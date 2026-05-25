package com.aicloud.module.pay.biz.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class CreatePayOrderRequest {
    @NotNull
    private Long tenantId;
    @NotNull
    private Long tradeOrderId;
    @NotBlank
    private String payOrderNo;
    @NotBlank
    private String channel;
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal amount;
    @NotBlank
    private String status;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getTradeOrderId() { return tradeOrderId; }
    public void setTradeOrderId(Long tradeOrderId) { this.tradeOrderId = tradeOrderId; }
    public String getPayOrderNo() { return payOrderNo; }
    public void setPayOrderNo(String payOrderNo) { this.payOrderNo = payOrderNo; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
