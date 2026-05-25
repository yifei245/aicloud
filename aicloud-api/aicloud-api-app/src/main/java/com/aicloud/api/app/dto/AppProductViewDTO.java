package com.aicloud.api.app.dto;

import java.math.BigDecimal;

/**
 * App product view DTO.
 *
 * @author yifei
 */
public class AppProductViewDTO {

    private Long spuId;
    private String spuNo;
    private String name;
    private String subTitle;
    private String coverUrl;
    private BigDecimal salePrice;
    private Integer stock;
    private String brandName;

    public Long getSpuId() { return spuId; }
    public void setSpuId(Long spuId) { this.spuId = spuId; }
    public String getSpuNo() { return spuNo; }
    public void setSpuNo(String spuNo) { this.spuNo = spuNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubTitle() { return subTitle; }
    public void setSubTitle(String subTitle) { this.subTitle = subTitle; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    
    public String toString() {
        return "AppProductViewDTO{}";
    }
}
