package com.aicloud.module.pay.biz.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class AppPayOrderCreateRequest {
    @NotNull
    private Long tradeOrderId;
    @NotBlank
    private String channel;
    public Long getTradeOrderId() { return tradeOrderId; }
    public void setTradeOrderId(Long tradeOrderId) { this.tradeOrderId = tradeOrderId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}
