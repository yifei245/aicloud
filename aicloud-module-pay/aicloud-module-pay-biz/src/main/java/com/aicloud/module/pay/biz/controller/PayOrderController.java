package com.aicloud.module.pay.biz.controller;

import com.aicloud.module.pay.biz.entity.AiPayChannel;
import com.aicloud.module.pay.biz.entity.AiPayOrder;
import com.aicloud.module.pay.biz.entity.AiPayRefund;
import com.aicloud.common.pojo.ApiResponse;
import com.aicloud.module.pay.biz.model.AppPayOrderCreateRequest;
import com.aicloud.module.pay.biz.model.CreatePayOrderRequest;
import com.aicloud.common.pojo.PageResponse;
import com.aicloud.module.pay.biz.model.PayChannelSaveRequest;
import com.aicloud.module.pay.biz.model.PayRefundCreateRequest;
import com.aicloud.module.pay.biz.model.PaySuccessNotifyRequest;
import com.aicloud.module.pay.biz.model.UpdatePayOrderStatusRequest;
import com.aicloud.module.pay.biz.service.PayOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Tag(name = "支付中心")
@RestController
@RequestMapping("/pay")
public class PayOrderController {

    private static final String USER_TYPE_MEMBER = "MEMBER";

    private final PayOrderService payOrderService;

    public PayOrderController(PayOrderService payOrderService) {
        this.payOrderService = payOrderService;
    }

    @Operation(summary = "管理端支付单分页")
    @GetMapping("/order/list")
    public ApiResponse<PageResponse<AiPayOrder>> list(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") long pageSize) {
        return ApiResponse.ok(payOrderService.listAdmin(tenantId, status, pageNo, pageSize));
    }

    @Operation(summary = "管理端创建支付单")
    @PostMapping("/order/create")
    public ApiResponse<AiPayOrder> create(@Valid @RequestBody CreatePayOrderRequest body) {
        return ApiResponse.ok(payOrderService.createAdmin(body));
    }

    @Operation(summary = "支付单详情")
    @GetMapping("/order/{id}")
    public ApiResponse<AiPayOrder> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(payOrderService.get(id));
    }

    @Operation(summary = "更新支付单状态")
    @PutMapping("/order/status")
    public ApiResponse<AiPayOrder> updateStatus(@Valid @RequestBody UpdatePayOrderStatusRequest body) {
        return ApiResponse.ok(payOrderService.updateStatus(body));
    }

    @Operation(summary = "会员端创建支付单")
    @PostMapping("/app/order/create")
    public ApiResponse<AiPayOrder> createMember(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @Valid @RequestBody AppPayOrderCreateRequest body) {
        validateMember(userType, userId);
        return ApiResponse.ok(payOrderService.createMember(parseLong(tenantId, 1L), parseLong(userId, null), body));
    }

    @Operation(summary = "会员端支付单列表")
    @GetMapping("/app/order/list")
    public ApiResponse<List<AiPayOrder>> memberList(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType) {
        validateMember(userType, userId);
        return ApiResponse.ok(payOrderService.listMember(parseLong(tenantId, 1L), parseLong(userId, null)));
    }

    @Operation(summary = "模拟支付成功回调")
    @PostMapping("/order/notify/success")
    public ApiResponse<AiPayOrder> notifySuccess(@Valid @RequestBody PaySuccessNotifyRequest body) {
        return ApiResponse.ok(payOrderService.notifySuccess(body.getPayOrderId()));
    }

    @Operation(summary = "支付渠道列表")
    @GetMapping("/channel/list")
    public ApiResponse<List<AiPayChannel>> channelList(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "status", required = false) Integer status) {
        return ApiResponse.ok(payOrderService.channelList(tenantId, status));
    }

    @Operation(summary = "创建或更新支付渠道")
    @PostMapping({"/channel/create", "/channel/update"})
    public ApiResponse<AiPayChannel> saveChannel(@Valid @RequestBody PayChannelSaveRequest body) {
        return ApiResponse.ok(payOrderService.saveChannel(body));
    }

    @Operation(summary = "退款单列表")
    @GetMapping("/refund/list")
    public ApiResponse<List<AiPayRefund>> refundList(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(payOrderService.refundList(tenantId, status));
    }

    @Operation(summary = "创建退款单")
    @PostMapping("/refund/create")
    public ApiResponse<AiPayRefund> createRefund(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @Valid @RequestBody PayRefundCreateRequest body) {
        return ApiResponse.ok(payOrderService.createRefund(tenantId, body));
    }

    @Operation(summary = "模拟退款成功回调")
    @PostMapping("/refund/notify/success")
    public ApiResponse<AiPayRefund> notifyRefundSuccess(@RequestParam Long id) {
        return ApiResponse.ok(payOrderService.notifyRefundSuccess(id));
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
