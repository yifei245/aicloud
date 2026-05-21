package com.aicloud.module.trade.biz.controller;

import com.aicloud.module.trade.biz.entity.AiTradeAfterSale;
import com.aicloud.module.trade.biz.entity.AiTradeCartItem;
import com.aicloud.module.trade.biz.entity.AiTradeDelivery;
import com.aicloud.module.trade.biz.entity.AiTradeOrder;
import com.aicloud.module.trade.biz.model.AfterSaleApplyRequest;
import com.aicloud.module.trade.biz.model.ApiResponse;
import com.aicloud.module.trade.biz.model.AppTradeOrderCreateRequest;
import com.aicloud.module.trade.biz.model.CartAddRequest;
import com.aicloud.module.trade.biz.model.CreateTradeOrderRequest;
import com.aicloud.module.trade.biz.model.DeliveryShipRequest;
import com.aicloud.module.trade.biz.model.PageResponse;
import com.aicloud.module.trade.biz.model.UpdateTradeOrderStatusRequest;
import com.aicloud.module.trade.biz.service.TradeOrderService;
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

@Tag(name = "交易中心")
@RestController
@RequestMapping("/trade")
public class TradeOrderController {
    private final TradeOrderService tradeOrderService;

    public TradeOrderController(TradeOrderService tradeOrderService) {
        this.tradeOrderService = tradeOrderService;
    }

    @Operation(summary = "管理端订单分页")
    @GetMapping("/order/list")
    public ApiResponse<PageResponse<AiTradeOrder>> list(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") long pageSize) {
        return ApiResponse.ok(tradeOrderService.listAdmin(tenantId, userId, status, pageNo, pageSize));
    }

    @Operation(summary = "管理端创建订单")
    @PostMapping("/order/create")
    public ApiResponse<AiTradeOrder> create(@Valid @RequestBody CreateTradeOrderRequest body) {
        return ApiResponse.ok(tradeOrderService.createAdmin(body));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/order/{id}")
    public ApiResponse<AiTradeOrder> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(tradeOrderService.get(id));
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/order/status")
    public ApiResponse<AiTradeOrder> updateStatus(@Valid @RequestBody UpdateTradeOrderStatusRequest body) {
        return ApiResponse.ok(tradeOrderService.updateStatus(body));
    }

    @Operation(summary = "会员端下单")
    @PostMapping("/app/order/create")
    public ApiResponse<AiTradeOrder> createMember(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @Valid @RequestBody AppTradeOrderCreateRequest body) {
        validateMember(userType, userId);
        return ApiResponse.ok(tradeOrderService.createMember(parseLong(tenantId, 1L), parseLong(userId, null), body));
    }

    @Operation(summary = "会员端订单列表")
    @GetMapping("/app/order/list")
    public ApiResponse<List<AiTradeOrder>> memberList(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType) {
        validateMember(userType, userId);
        return ApiResponse.ok(tradeOrderService.listMember(parseLong(tenantId, 1L), parseLong(userId, null)));
    }

    @Operation(summary = "会员端取消订单")
    @PostMapping("/app/order/cancel")
    public ApiResponse<AiTradeOrder> cancel(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestParam("id") Long id) {
        validateMember(userType, userId);
        return ApiResponse.ok(tradeOrderService.cancel(parseLong(tenantId, 1L), parseLong(userId, null), id));
    }

    @Operation(summary = "会员端确认收货")
    @PostMapping("/app/order/confirm")
    public ApiResponse<AiTradeOrder> confirm(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestParam("id") Long id) {
        validateMember(userType, userId);
        return ApiResponse.ok(tradeOrderService.confirm(parseLong(tenantId, 1L), parseLong(userId, null), id));
    }

    @Operation(summary = "会员购物车列表")
    @GetMapping("/app/cart/list")
    public ApiResponse<List<AiTradeCartItem>> cartList(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType) {
        validateMember(userType, userId);
        return ApiResponse.ok(tradeOrderService.cartList(parseLong(tenantId, 1L), parseLong(userId, null)));
    }

    @Operation(summary = "加入购物车")
    @PostMapping("/app/cart/add")
    public ApiResponse<AiTradeCartItem> addCart(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @Valid @RequestBody CartAddRequest body) {
        validateMember(userType, userId);
        return ApiResponse.ok(tradeOrderService.addCart(parseLong(tenantId, 1L), parseLong(userId, null), body));
    }

    @Operation(summary = "修改购物车数量")
    @PostMapping("/app/cart/quantity")
    public ApiResponse<AiTradeCartItem> updateCartQuantity(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestParam Long id,
            @RequestParam Integer quantity) {
        validateMember(userType, userId);
        return ApiResponse.ok(tradeOrderService.updateCartQuantity(parseLong(tenantId, 1L), parseLong(userId, null), id, quantity));
    }

    @Operation(summary = "删除购物车项")
    @PostMapping("/app/cart/delete")
    public ApiResponse<Boolean> deleteCart(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @RequestParam Long id) {
        validateMember(userType, userId);
        tradeOrderService.deleteCart(parseLong(tenantId, 1L), parseLong(userId, null), id);
        return ApiResponse.ok(Boolean.TRUE);
    }

    @Operation(summary = "物流列表")
    @GetMapping("/delivery/list")
    public ApiResponse<List<AiTradeDelivery>> deliveryList(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "orderId", required = false) Long orderId) {
        return ApiResponse.ok(tradeOrderService.deliveryList(tenantId, orderId));
    }

    @Operation(summary = "订单发货")
    @PostMapping("/delivery/ship")
    public ApiResponse<AiTradeDelivery> ship(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @Valid @RequestBody DeliveryShipRequest body) {
        return ApiResponse.ok(tradeOrderService.ship(tenantId, body));
    }

    @Operation(summary = "物流签收")
    @PostMapping("/delivery/receive")
    public ApiResponse<AiTradeDelivery> receive(@RequestParam Long id) {
        return ApiResponse.ok(tradeOrderService.receiveDelivery(id));
    }

    @Operation(summary = "售后列表")
    @GetMapping("/after-sale/list")
    public ApiResponse<List<AiTradeAfterSale>> afterSaleList(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(tradeOrderService.afterSaleList(tenantId, status));
    }

    @Operation(summary = "会员申请售后")
    @PostMapping("/app/after-sale/apply")
    public ApiResponse<AiTradeAfterSale> applyAfterSale(@RequestHeader(name = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @RequestHeader(name = "X-User-Type", required = false) String userType,
            @Valid @RequestBody AfterSaleApplyRequest body) {
        validateMember(userType, userId);
        return ApiResponse.ok(tradeOrderService.applyAfterSale(parseLong(tenantId, 1L), parseLong(userId, null), body));
    }

    @Operation(summary = "售后审核")
    @PostMapping("/after-sale/audit")
    public ApiResponse<AiTradeAfterSale> auditAfterSale(@RequestParam Long id, @RequestParam String status,
            @RequestParam(required = false) String auditRemark) {
        return ApiResponse.ok(tradeOrderService.auditAfterSale(id, status, auditRemark));
    }

    private void validateMember(String userType, String userId) {
        if (!"MEMBER".equalsIgnoreCase(userType)) {
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
