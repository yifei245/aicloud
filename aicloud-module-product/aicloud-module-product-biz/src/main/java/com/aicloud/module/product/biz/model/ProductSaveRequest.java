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
public class ProductSaveRequest {
    private Long id;
    @NotNull
    private Long tenantId;
    private String spuNo;
    @NotBlank
    private String name;
    private String subTitle;
    private Long categoryId;
    private String brandName;
    private String unitName;
    private String coverUrl;
    @NotNull
    private Integer status;
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal salePrice;
    @NotNull
    @Min(0)
    private Integer stock;
    private Integer sort;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getSpuNo() { return spuNo; }
    public void setSpuNo(String spuNo) { this.spuNo = spuNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubTitle() { return subTitle; }
    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public java.math.BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(java.math.BigDecimal salePrice) { this.salePrice = salePrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
