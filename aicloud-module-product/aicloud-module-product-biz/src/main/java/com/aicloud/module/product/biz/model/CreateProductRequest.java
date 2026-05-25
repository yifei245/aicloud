package com.aicloud.module.product.biz.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class CreateProductRequest {
    @NotNull
    private Long tenantId;
    @NotBlank
    private String name;
    private Long categoryId;
    @NotNull
    private Integer status;
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal salePrice;
    @NotNull
    @Min(0)
    private Integer stock;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
