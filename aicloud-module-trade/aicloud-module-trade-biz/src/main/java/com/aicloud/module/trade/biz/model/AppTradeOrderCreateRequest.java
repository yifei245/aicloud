package com.aicloud.module.trade.biz.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class AppTradeOrderCreateRequest {
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal totalAmount;
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal payAmount;

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
}
