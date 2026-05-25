package com.aicloud.api.web.dto;

import java.util.List;

/**
 * Web order submit DTO.
 *
 * @author yifei
 */
public class WebOrderSubmitDTO {

    private Long tenantId;
    private Long userId;
    private List<Long> cartItemIds;
    private Long addressId;
    private String couponCode;
    private String remark;

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<Long> getCartItemIds() { return cartItemIds; }
    public void setCartItemIds(List<Long> cartItemIds) { this.cartItemIds = cartItemIds; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    
    public String toString() {
        return "WebOrderSubmitDTO{}";
    }
}
