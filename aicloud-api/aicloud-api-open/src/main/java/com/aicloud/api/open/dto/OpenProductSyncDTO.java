package com.aicloud.api.open.dto;

import java.math.BigDecimal;

/**
 * OpenAPI product sync DTO.
 *
 * @author yifei
 */
public class OpenProductSyncDTO extends OpenApiBaseRequest {

    private String outerProductId;
    private String productName;
    private String categoryCode;
    private BigDecimal salePrice;
    private Integer stock;
    private String status;

    public String getOuterProductId() { return outerProductId; }
    public void setOuterProductId(String outerProductId) { this.outerProductId = outerProductId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    
    public String toString() {
        return "OpenProductSyncDTO{}";
    }
}
