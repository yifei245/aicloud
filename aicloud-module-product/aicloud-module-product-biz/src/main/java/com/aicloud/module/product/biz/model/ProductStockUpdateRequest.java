package com.aicloud.module.product.biz.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ProductStockUpdateRequest {
    @NotNull
    private Long id;
    @NotNull
    @Min(0)
    private Integer stock;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
