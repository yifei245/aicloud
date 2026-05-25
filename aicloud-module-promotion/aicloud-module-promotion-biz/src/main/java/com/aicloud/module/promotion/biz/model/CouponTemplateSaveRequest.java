package com.aicloud.module.promotion.biz.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class CouponTemplateSaveRequest {
    private Long id;
    @NotNull
    private Long tenantId;
    private String templateNo;
    @NotBlank
    private String name;
    @NotBlank
    private String discountType;
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal discountValue;
    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal minAmount;
    @NotNull
    @Min(0)
    private Integer totalCount;
    @NotNull
    @Min(1)
    private Integer receiveLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @NotNull
    private Integer status;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getTemplateNo() { return templateNo; }
    public void setTemplateNo(String templateNo) { this.templateNo = templateNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getReceiveLimit() { return receiveLimit; }
    public void setReceiveLimit(Integer receiveLimit) { this.receiveLimit = receiveLimit; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
