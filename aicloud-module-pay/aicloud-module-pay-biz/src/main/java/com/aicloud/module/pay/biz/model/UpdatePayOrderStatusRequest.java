package com.aicloud.module.pay.biz.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class UpdatePayOrderStatusRequest {
    @NotNull
    private Long id;
    @NotBlank
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
