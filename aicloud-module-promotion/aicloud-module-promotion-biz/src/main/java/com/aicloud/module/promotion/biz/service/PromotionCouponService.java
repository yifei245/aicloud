package com.aicloud.module.promotion.biz.service;

import com.aicloud.module.promotion.biz.entity.AiPromotionCouponTemplate;
import com.aicloud.module.promotion.biz.entity.AiPromotionUserCoupon;
import com.aicloud.module.promotion.biz.mapper.CouponTemplateMapper;
import com.aicloud.module.promotion.biz.mapper.UserCouponMapper;
import com.aicloud.module.promotion.biz.model.CouponTemplateSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class PromotionCouponService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;

    public PromotionCouponService(CouponTemplateMapper couponTemplateMapper, UserCouponMapper userCouponMapper) {
        this.couponTemplateMapper = couponTemplateMapper;
        this.userCouponMapper = userCouponMapper;
    }

    public List<AiPromotionCouponTemplate> listTemplates(Long tenantId, Integer status) {
        return couponTemplateMapper.selectList(new LambdaQueryWrapper<AiPromotionCouponTemplate>()
                .eq(AiPromotionCouponTemplate::getTenantId, tenantId)
                .eq(status != null, AiPromotionCouponTemplate::getStatus, status)
                .orderByDesc(AiPromotionCouponTemplate::getId));
    }

    public AiPromotionCouponTemplate getTemplate(Long id) {
        AiPromotionCouponTemplate template = couponTemplateMapper.selectById(id);
        if (template == null) {
            throw new IllegalArgumentException("优惠券模板不存在");
        }
        return template;
    }

    public AiPromotionCouponTemplate saveTemplate(CouponTemplateSaveRequest request) {
        AiPromotionCouponTemplate template = request.getId() == null ? new AiPromotionCouponTemplate() : getTemplate(request.getId());
        template.setTenantId(request.getTenantId());
        template.setTemplateNo(StringUtils.hasText(request.getTemplateNo()) ? request.getTemplateNo() : defaultTemplateNo());
        template.setName(request.getName());
        template.setDiscountType(request.getDiscountType().toUpperCase());
        template.setDiscountValue(request.getDiscountValue());
        template.setMinAmount(request.getMinAmount());
        template.setTotalCount(request.getTotalCount());
        if (template.getClaimCount() == null) {
            template.setClaimCount(0);
        }
        template.setReceiveLimit(request.getReceiveLimit());
        template.setStartTime(request.getStartTime());
        template.setEndTime(request.getEndTime());
        template.setStatus(request.getStatus());
        template.setRemark(request.getRemark());
        template.setUpdateTime(LocalDateTime.now());
        if (template.getId() == null) {
            template.setCreateTime(LocalDateTime.now());
            couponTemplateMapper.insert(template);
        } else {
            couponTemplateMapper.updateById(template);
        }
        return template;
    }

    public AiPromotionCouponTemplate updateTemplateStatus(Long id, Integer status) {
        AiPromotionCouponTemplate template = getTemplate(id);
        template.setStatus(status);
        template.setUpdateTime(LocalDateTime.now());
        couponTemplateMapper.updateById(template);
        return template;
    }

    public void deleteTemplate(Long id) {
        AiPromotionCouponTemplate template = getTemplate(id);
        long couponCount = userCouponMapper.selectCount(new LambdaQueryWrapper<AiPromotionUserCoupon>()
                .eq(AiPromotionUserCoupon::getTemplateId, template.getId()));
        if (couponCount > 0) {
            throw new IllegalArgumentException("模板已有领取记录，不能删除");
        }
        couponTemplateMapper.deleteById(id);
    }

    public Map<String, Object> preview(Long templateId, BigDecimal amount) {
        AiPromotionCouponTemplate template = getTemplate(templateId);
        if (amount.compareTo(nullToZero(template.getMinAmount())) < 0) {
            throw new IllegalArgumentException("未达到优惠券使用门槛");
        }
        BigDecimal payable;
        if ("DISCOUNT".equalsIgnoreCase(template.getDiscountType())) {
            payable = amount.multiply(template.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            payable = amount.subtract(template.getDiscountValue()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal discount = amount.subtract(payable).setScale(2, RoundingMode.HALF_UP);
        Map<String, Object> data = new HashMap<>();
        data.put("templateId", templateId);
        data.put("templateName", template.getName());
        data.put("originAmount", amount);
        data.put("discountType", template.getDiscountType());
        data.put("discountValue", template.getDiscountValue());
        data.put("discountAmount", discount);
        data.put("payableAmount", payable);
        return data;
    }

    public List<AiPromotionUserCoupon> listUserCoupons(Long tenantId, Long userId, String status) {
        return userCouponMapper.selectList(new LambdaQueryWrapper<AiPromotionUserCoupon>()
                .eq(AiPromotionUserCoupon::getTenantId, tenantId)
                .eq(userId != null, AiPromotionUserCoupon::getUserId, userId)
                .eq(StringUtils.hasText(status), AiPromotionUserCoupon::getStatus, status)
                .orderByDesc(AiPromotionUserCoupon::getId));
    }

    public AiPromotionUserCoupon claim(Long tenantId, Long userId, Long templateId) {
        AiPromotionCouponTemplate template = getTemplate(templateId);
        validateClaimable(template);
        long owned = userCouponMapper.selectCount(new LambdaQueryWrapper<AiPromotionUserCoupon>()
                .eq(AiPromotionUserCoupon::getTenantId, tenantId)
                .eq(AiPromotionUserCoupon::getUserId, userId)
                .eq(AiPromotionUserCoupon::getTemplateId, templateId));
        if (owned >= template.getReceiveLimit()) {
            throw new IllegalArgumentException("已达到领取上限");
        }
        if (template.getClaimCount() >= template.getTotalCount()) {
            throw new IllegalArgumentException("优惠券已领完");
        }
        AiPromotionUserCoupon coupon = new AiPromotionUserCoupon();
        coupon.setTenantId(tenantId);
        coupon.setTemplateId(templateId);
        coupon.setUserId(userId);
        coupon.setCouponCode(defaultCouponCode());
        coupon.setStatus("UNUSED");
        coupon.setClaimTime(LocalDateTime.now());
        coupon.setExpireTime(template.getEndTime());
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setUpdateTime(LocalDateTime.now());
        userCouponMapper.insert(coupon);
        template.setClaimCount(template.getClaimCount() + 1);
        template.setUpdateTime(LocalDateTime.now());
        couponTemplateMapper.updateById(template);
        return coupon;
    }

    public AiPromotionUserCoupon useCoupon(Long id) {
        AiPromotionUserCoupon coupon = getUserCoupon(id);
        if (!"UNUSED".equals(coupon.getStatus())) {
            throw new IllegalArgumentException("优惠券不是未使用状态");
        }
        if (coupon.getExpireTime() != null && coupon.getExpireTime().isBefore(LocalDateTime.now())) {
            coupon.setStatus("EXPIRED");
            coupon.setUpdateTime(LocalDateTime.now());
            userCouponMapper.updateById(coupon);
            throw new IllegalArgumentException("优惠券已过期");
        }
        coupon.setStatus("USED");
        coupon.setUseTime(LocalDateTime.now());
        coupon.setUpdateTime(LocalDateTime.now());
        userCouponMapper.updateById(coupon);
        return coupon;
    }

    public AiPromotionUserCoupon cancelCoupon(Long id) {
        AiPromotionUserCoupon coupon = getUserCoupon(id);
        if ("USED".equals(coupon.getStatus())) {
            throw new IllegalArgumentException("已使用优惠券不能作废");
        }
        coupon.setStatus("CANCELLED");
        coupon.setUpdateTime(LocalDateTime.now());
        userCouponMapper.updateById(coupon);
        return coupon;
    }

    private AiPromotionUserCoupon getUserCoupon(Long id) {
        AiPromotionUserCoupon coupon = userCouponMapper.selectById(id);
        if (coupon == null) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        return coupon;
    }

    private void validateClaimable(AiPromotionCouponTemplate template) {
        LocalDateTime now = LocalDateTime.now();
        if (template.getStatus() == null || template.getStatus() != 1) {
            throw new IllegalArgumentException("优惠券模板已停用");
        }
        if (template.getStartTime() != null && template.getStartTime().isAfter(now)) {
            throw new IllegalArgumentException("优惠券活动未开始");
        }
        if (template.getEndTime() != null && template.getEndTime().isBefore(now)) {
            throw new IllegalArgumentException("优惠券活动已结束");
        }
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String defaultTemplateNo() {
        return "TPL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String defaultCouponCode() {
        return "CPN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
