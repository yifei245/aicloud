package com.aicloud.module.pay.biz.model;

import jakarta.validation.constraints.NotNull;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class PaySuccessNotifyRequest {
    @NotNull
    private Long payOrderId;
    public Long getPayOrderId() { return payOrderId; }
    public void setPayOrderId(Long payOrderId) { this.payOrderId = payOrderId; }
}
