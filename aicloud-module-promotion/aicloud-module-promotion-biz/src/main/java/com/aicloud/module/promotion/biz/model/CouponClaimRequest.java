package com.aicloud.module.promotion.biz.model;

import jakarta.validation.constraints.NotNull;

public class CouponClaimRequest {
    @NotNull
    private Long templateId;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
}
