package com.aicloud.module.product.biz.model;

import jakarta.validation.constraints.NotNull;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class ProductStatusUpdateRequest {
    @NotNull
    private Long id;
    @NotNull
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
