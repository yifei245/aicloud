package com.aicloud.api.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Web promotion DTO.
 *
 * @author yifei
 */
public class WebPromotionDTO {

    private Long id;
    private String name;
    private String promotionType;
    private BigDecimal discountAmount;
    private BigDecimal minAmount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPromotionType() { return promotionType; }
    public void setPromotionType(String promotionType) { this.promotionType = promotionType; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    
    public String toString() {
        return "WebPromotionDTO{}";
    }
}
