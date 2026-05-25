package com.aicloud.module.trade.biz.service;

import com.aicloud.module.trade.biz.entity.AiTradeAfterSale;
import com.aicloud.module.trade.biz.entity.AiTradeCartItem;
import com.aicloud.module.trade.biz.entity.AiTradeDelivery;
import com.aicloud.module.trade.biz.entity.AiTradeOrder;
import com.aicloud.module.trade.biz.mapper.TradeAfterSaleMapper;
import com.aicloud.module.trade.biz.mapper.TradeCartItemMapper;
import com.aicloud.module.trade.biz.mapper.TradeDeliveryMapper;
import com.aicloud.module.trade.biz.mapper.TradeOrderMapper;
import com.aicloud.module.trade.biz.model.AfterSaleApplyRequest;
import com.aicloud.module.trade.biz.model.AppTradeOrderCreateRequest;
import com.aicloud.module.trade.biz.model.CartAddRequest;
import com.aicloud.module.trade.biz.model.CreateTradeOrderRequest;
import com.aicloud.module.trade.biz.model.DeliveryShipRequest;
import com.aicloud.module.trade.biz.model.PageResponse;
import com.aicloud.module.trade.biz.model.UpdateTradeOrderStatusRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class TradeOrderService {

    private static final String ORDER_STATUS_CREATED = "CREATED";
    private static final String ORDER_STATUS_PAID = "PAID";
    private static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final String ORDER_STATUS_FINISHED = "FINISHED";
    private static final String ORDER_STATUS_AFTER_SALE = "AFTER_SALE";
    private static final String DELIVERY_STATUS_RECEIVED = "RECEIVED";
    private static final String AFTER_SALE_STATUS_APPLY = "APPLY";

    private final TradeOrderMapper tradeOrderMapper;
    private final TradeCartItemMapper cartItemMapper;
    private final TradeDeliveryMapper deliveryMapper;
    private final TradeAfterSaleMapper afterSaleMapper;

    public TradeOrderService(TradeOrderMapper tradeOrderMapper, TradeCartItemMapper cartItemMapper,
            TradeDeliveryMapper deliveryMapper, TradeAfterSaleMapper afterSaleMapper) {
        this.tradeOrderMapper = tradeOrderMapper;
        this.cartItemMapper = cartItemMapper;
        this.deliveryMapper = deliveryMapper;
        this.afterSaleMapper = afterSaleMapper;
    }

    public PageResponse<AiTradeOrder> listAdmin(Long tenantId, Long userId, String status, long pageNo, long pageSize) {
        Page<AiTradeOrder> page = new Page<>(Math.max(pageNo, 1), Math.min(Math.max(pageSize, 1), 100));
        LambdaQueryWrapper<AiTradeOrder> wrapper = new LambdaQueryWrapper<AiTradeOrder>()
                .eq(AiTradeOrder::getTenantId, tenantId)
                .eq(userId != null, AiTradeOrder::getUserId, userId)
                .eq(StringUtils.hasText(status), AiTradeOrder::getStatus, status)
                .orderByDesc(AiTradeOrder::getId);
        Page<AiTradeOrder> result = tradeOrderMapper.selectPage(page, wrapper);
        PageResponse<AiTradeOrder> data = new PageResponse<>();
        data.setTotal(result.getTotal());
        data.setPageNo(result.getCurrent());
        data.setPageSize(result.getSize());
        data.setList(result.getRecords());
        return data;
    }

    public List<AiTradeOrder> listMember(Long tenantId, Long userId) {
        return tradeOrderMapper.selectList(new LambdaQueryWrapper<AiTradeOrder>()
                .eq(AiTradeOrder::getTenantId, tenantId)
                .eq(AiTradeOrder::getUserId, userId)
                .orderByDesc(AiTradeOrder::getId));
    }

    public AiTradeOrder createAdmin(CreateTradeOrderRequest body) {
        AiTradeOrder order = new AiTradeOrder();
        order.setTenantId(body.getTenantId());
        order.setOrderNo(StringUtils.hasText(body.getOrderNo()) ? body.getOrderNo() : defaultOrderNo());
        order.setUserId(body.getUserId());
        order.setTotalAmount(body.getTotalAmount());
        order.setPayAmount(body.getPayAmount());
        order.setStatus(StringUtils.hasText(body.getStatus()) ? body.getStatus() : ORDER_STATUS_CREATED);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        tradeOrderMapper.insert(order);
        return order;
    }

    public AiTradeOrder createMember(Long tenantId, Long userId, AppTradeOrderCreateRequest body) {
        AiTradeOrder order = new AiTradeOrder();
        order.setTenantId(tenantId);
        order.setOrderNo(defaultOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(body.getTotalAmount());
        order.setPayAmount(body.getPayAmount());
        order.setStatus(ORDER_STATUS_CREATED);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        tradeOrderMapper.insert(order);
        return order;
    }

    public AiTradeOrder get(Long id) {
        AiTradeOrder order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    public AiTradeOrder updateStatus(UpdateTradeOrderStatusRequest body) {
        AiTradeOrder order = get(body.getId());
        order.setStatus(body.getStatus());
        order.setUpdateTime(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
        return order;
    }

    public AiTradeOrder cancel(Long tenantId, Long userId, Long id) {
        AiTradeOrder order = validateMemberOrder(tenantId, userId, id);
        if (ORDER_STATUS_PAID.equals(order.getStatus()) || ORDER_STATUS_SHIPPED.equals(order.getStatus())) {
            throw new IllegalArgumentException("订单已支付或已发货，请申请售后");
        }
        order.setStatus(ORDER_STATUS_CANCELLED);
        order.setUpdateTime(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
        return order;
    }

    public AiTradeOrder confirm(Long tenantId, Long userId, Long id) {
        AiTradeOrder order = validateMemberOrder(tenantId, userId, id);
        order.setStatus(ORDER_STATUS_FINISHED);
        order.setUpdateTime(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
        return order;
    }

    public List<AiTradeCartItem> cartList(Long tenantId, Long userId) {
        return cartItemMapper.selectList(new LambdaQueryWrapper<AiTradeCartItem>()
                .eq(AiTradeCartItem::getTenantId, tenantId)
                .eq(AiTradeCartItem::getUserId, userId)
                .orderByDesc(AiTradeCartItem::getId));
    }

    public AiTradeCartItem addCart(Long tenantId, Long userId, CartAddRequest body) {
        AiTradeCartItem item = cartItemMapper.selectOne(new LambdaQueryWrapper<AiTradeCartItem>()
                .eq(AiTradeCartItem::getTenantId, tenantId)
                .eq(AiTradeCartItem::getUserId, userId)
                .eq(AiTradeCartItem::getSpuId, body.getSpuId()));
        if (item == null) {
            item = new AiTradeCartItem();
            item.setTenantId(tenantId);
            item.setUserId(userId);
            item.setSpuId(body.getSpuId());
            item.setSpuName(body.getSpuName());
            item.setPrice(body.getPrice());
            item.setQuantity(body.getQuantity());
            item.setSelected(1);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            cartItemMapper.insert(item);
        } else {
            item.setQuantity(item.getQuantity() + body.getQuantity());
            item.setPrice(body.getPrice());
            item.setSpuName(body.getSpuName());
            item.setUpdateTime(LocalDateTime.now());
            cartItemMapper.updateById(item);
        }
        return item;
    }

    public AiTradeCartItem updateCartQuantity(Long tenantId, Long userId, Long id, Integer quantity) {
        AiTradeCartItem item = getCartItem(tenantId, userId, id);
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        item.setQuantity(quantity);
        item.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(item);
        return item;
    }

    public void deleteCart(Long tenantId, Long userId, Long id) {
        getCartItem(tenantId, userId, id);
        cartItemMapper.deleteById(id);
    }

    public List<AiTradeDelivery> deliveryList(Long tenantId, Long orderId) {
        return deliveryMapper.selectList(new LambdaQueryWrapper<AiTradeDelivery>()
                .eq(AiTradeDelivery::getTenantId, tenantId)
                .eq(orderId != null, AiTradeDelivery::getOrderId, orderId)
                .orderByDesc(AiTradeDelivery::getId));
    }

    public AiTradeDelivery ship(Long tenantId, DeliveryShipRequest body) {
        AiTradeOrder order = get(body.getOrderId());
        if (!tenantId.equals(order.getTenantId())) {
            throw new IllegalArgumentException("租户不匹配");
        }
        AiTradeDelivery delivery = new AiTradeDelivery();
        delivery.setTenantId(tenantId);
        delivery.setOrderId(body.getOrderId());
        delivery.setDeliveryNo(StringUtils.hasText(body.getDeliveryNo()) ? body.getDeliveryNo() : defaultDeliveryNo());
        delivery.setCompanyCode(body.getCompanyCode());
        delivery.setCompanyName(body.getCompanyName());
        delivery.setReceiverName(body.getReceiverName());
        delivery.setReceiverMobile(body.getReceiverMobile());
        delivery.setReceiverAddress(body.getReceiverAddress());
        delivery.setStatus(ORDER_STATUS_SHIPPED);
        delivery.setShippedTime(LocalDateTime.now());
        delivery.setCreateTime(LocalDateTime.now());
        delivery.setUpdateTime(LocalDateTime.now());
        deliveryMapper.insert(delivery);
        order.setStatus(ORDER_STATUS_SHIPPED);
        order.setUpdateTime(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
        return delivery;
    }

    public AiTradeDelivery receiveDelivery(Long id) {
        AiTradeDelivery delivery = deliveryMapper.selectById(id);
        if (delivery == null) {
            throw new IllegalArgumentException("物流单不存在");
        }
        delivery.setStatus(DELIVERY_STATUS_RECEIVED);
        delivery.setReceivedTime(LocalDateTime.now());
        delivery.setUpdateTime(LocalDateTime.now());
        deliveryMapper.updateById(delivery);
        AiTradeOrder order = get(delivery.getOrderId());
        order.setStatus(ORDER_STATUS_FINISHED);
        order.setUpdateTime(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
        return delivery;
    }

    public List<AiTradeAfterSale> afterSaleList(Long tenantId, String status) {
        return afterSaleMapper.selectList(new LambdaQueryWrapper<AiTradeAfterSale>()
                .eq(AiTradeAfterSale::getTenantId, tenantId)
                .eq(StringUtils.hasText(status), AiTradeAfterSale::getStatus, status)
                .orderByDesc(AiTradeAfterSale::getId));
    }

    public AiTradeAfterSale applyAfterSale(Long tenantId, Long userId, AfterSaleApplyRequest body) {
        AiTradeOrder order = validateMemberOrder(tenantId, userId, body.getOrderId());
        AiTradeAfterSale afterSale = new AiTradeAfterSale();
        afterSale.setTenantId(tenantId);
        afterSale.setAfterSaleNo(defaultAfterSaleNo());
        afterSale.setOrderId(order.getId());
        afterSale.setUserId(userId);
        afterSale.setType(body.getType());
        afterSale.setReason(body.getReason());
        afterSale.setAmount(body.getAmount());
        afterSale.setStatus(AFTER_SALE_STATUS_APPLY);
        afterSale.setCreateTime(LocalDateTime.now());
        afterSale.setUpdateTime(LocalDateTime.now());
        afterSaleMapper.insert(afterSale);
        order.setStatus(ORDER_STATUS_AFTER_SALE);
        order.setUpdateTime(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
        return afterSale;
    }

    public AiTradeAfterSale auditAfterSale(Long id, String status, String auditRemark) {
        AiTradeAfterSale afterSale = afterSaleMapper.selectById(id);
        if (afterSale == null) {
            throw new IllegalArgumentException("售后单不存在");
        }
        afterSale.setStatus(status);
        afterSale.setAuditRemark(auditRemark);
        afterSale.setUpdateTime(LocalDateTime.now());
        afterSaleMapper.updateById(afterSale);
        return afterSale;
    }

    private AiTradeOrder validateMemberOrder(Long tenantId, Long userId, Long id) {
        AiTradeOrder order = get(id);
        if (!tenantId.equals(order.getTenantId()) || !userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        return order;
    }

    private AiTradeCartItem getCartItem(Long tenantId, Long userId, Long id) {
        AiTradeCartItem item = cartItemMapper.selectById(id);
        if (item == null || !tenantId.equals(item.getTenantId()) || !userId.equals(item.getUserId())) {
            throw new IllegalArgumentException("购物车项不存在");
        }
        return item;
    }

    private String defaultOrderNo() {
        return "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String defaultDeliveryNo() {
        return "D-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String defaultAfterSaleNo() {
        return "AS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
