package com.aicloud.api.app.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * App order create DTO.
 *
 * @author yifei
 */
public class AppOrderCreateDTO {

    private Long tenantId;
    private Long userId;
    private List<Item> items;
    private String receiverName;
    private String receiverMobile;
    private String receiverAddress;
    private String remark;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverMobile() { return receiverMobile; }
    public void setReceiverMobile(String receiverMobile) { this.receiverMobile = receiverMobile; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public static class Item {
        private Long spuId;
        private Long skuId;
        private String spuName;
        private BigDecimal price;
        private Integer quantity;

        public Long getSpuId() { return spuId; }
        public void setSpuId(Long spuId) { this.spuId = spuId; }
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        public String getSpuName() { return spuName; }
        public void setSpuName(String spuName) { this.spuName = spuName; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        
        public String toString() {
            return "Item{}";
        }
    }

    
    public String toString() {
        return "AppOrderCreateDTO{}";
    }
}
