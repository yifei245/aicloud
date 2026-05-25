package com.aicloud.module.promotion.biz.controller;

import com.aicloud.module.promotion.biz.entity.AiPromotionCouponTemplate;
import com.aicloud.module.promotion.biz.entity.AiPromotionUserCoupon;
import com.aicloud.common.pojo.ApiResponse;
import com.aicloud.module.promotion.biz.model.CouponClaimRequest;
import com.aicloud.module.promotion.biz.model.CouponTemplateSaveRequest;
import com.aicloud.module.promotion.biz.service.PromotionCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "营销中心")
@RestController
/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class PromotionController {

    private static final String USER_TYPE_MEMBER = "MEMBER";

    private final PromotionCouponService promotionCouponService;

    public PromotionController(PromotionCouponService promotionCouponService) {
        this.promotionCouponService = promotionCouponService;
    }

    @Operation(summary = "优惠券模板列表")
    @GetMapping("/promotion/coupon/template/list")
    public ApiResponse<List<AiPromotionCouponTemplate>> templateList(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(promotionCouponService.listTemplates(tenantId, status));
    }

    @Operation(summary = "优惠券模板详情")
    @GetMapping("/promotion/coupon/template/{id}")
    public ApiResponse<AiPromotionCouponTemplate> getTemplate(@PathVariable("id") Long id) {
        return ApiResponse.ok(promotionCouponService.getTemplate(id));
    }

    @Operation(summary = "创建或更新优惠券模板")
    @PostMapping({"/promotion/coupon/template/create", "/promotion/coupon/template/update"})
    public ApiResponse<AiPromotionCouponTemplate> saveTemplate(@Valid @RequestBody CouponTemplateSaveRequest body) {
        return ApiResponse.ok(promotionCouponService.saveTemplate(body));
    }

    @Operation(summary = "优惠券模板启停")
    @PostMapping("/promotion/coupon/template/status")
    public ApiResponse<AiPromotionCouponTemplate> updateTemplateStatus(@RequestParam("id") Long id, @RequestParam Integer status) {
        return ApiResponse.ok(promotionCouponService.updateTemplateStatus(id, status));
    }

    @Operation(summary = "删除优惠券模板")
    @DeleteMapping("/promotion/coupon/template/{id}")
    public ApiResponse<Boolean> deleteTemplate(@PathVariable("id") Long id) {
        promotionCouponService.deleteTemplate(id);
        return ApiResponse.ok(Boolean.TRUE);
    }

    @Operation(summary = "管理端会员券列表")
    @GetMapping("/promotion/coupon/user/list")
    public ApiResponse<List<AiPromotionUserCoupon>> adminUserCoupons(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(promotionCouponService.listUserCoupons(tenantId, userId, status));
    }

    @Operation(summary = "优惠券试算")
    @GetMapping({"/promotion/coupon/preview", "/app/promotion/coupon/preview", "/web/promotion/coupon/preview"})
    public ApiResponse<Map<String, Object>> couponPreview(@RequestParam("templateId") Long templateId,
            @RequestParam("amount") BigDecimal amount) {
        return ApiResponse.ok(promotionCouponService.preview(templateId, amount));
    }

    @Operation(summary = "会员优惠券列表")
    @GetMapping({"/app/promotion/coupon/list", "/web/promotion/coupon/list"})
    public ApiResponse<List<AiPromotionUserCoupon>> userCoupons(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestParam(name = "status", required = false) String status) {
        validateMember(userType, userId);
        return ApiResponse.ok(promotionCouponService.listUserCoupons(parseLong(tenantId, 1L), parseLong(userId, null), status));
    }

    @Operation(summary = "会员领取优惠券")
    @PostMapping({"/app/promotion/coupon/claim", "/web/promotion/coupon/claim"})
    public ApiResponse<AiPromotionUserCoupon> claim(
            @RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @Valid @RequestBody CouponClaimRequest body) {
        validateMember(userType, userId);
        return ApiResponse.ok(promotionCouponService.claim(parseLong(tenantId, 1L), parseLong(userId, null), body.getTemplateId()));
    }

    @Operation(summary = "核销优惠券")
    @PostMapping("/promotion/coupon/use")
    public ApiResponse<AiPromotionUserCoupon> useCoupon(@RequestParam("id") Long id) {
        return ApiResponse.ok(promotionCouponService.useCoupon(id));
    }

    @Operation(summary = "作废优惠券")
    @PostMapping("/promotion/coupon/cancel")
    public ApiResponse<AiPromotionUserCoupon> cancelCoupon(@RequestParam("id") Long id) {
        return ApiResponse.ok(promotionCouponService.cancelCoupon(id));
    }

    private void validateMember(String userType, String userId) {
        if (!USER_TYPE_MEMBER.equalsIgnoreCase(userType)) {
            throw new IllegalArgumentException("仅会员用户可访问");
        }
        if (parseLong(userId, null) == null) {
            throw new IllegalArgumentException("缺少用户上下文");
        }
    }

    private Long parseLong(String value, Long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
