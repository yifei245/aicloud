package com.aicloud.module.promotion.biz.model;

import jakarta.validation.constraints.NotNull;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class CouponClaimRequest {
    @NotNull
    private Long templateId;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
}
