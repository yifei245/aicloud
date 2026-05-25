package com.aicloud.module.pay.biz.service;

import com.aicloud.module.pay.biz.entity.AiPayChannel;
import com.aicloud.module.pay.biz.entity.AiPayOrder;
import com.aicloud.module.pay.biz.entity.AiPayRefund;
import com.aicloud.module.pay.biz.entity.AiTradeOrderRef;
import com.aicloud.module.pay.biz.mapper.PayChannelMapper;
import com.aicloud.module.pay.biz.mapper.PayOrderMapper;
import com.aicloud.module.pay.biz.mapper.PayRefundMapper;
import com.aicloud.module.pay.biz.mapper.TradeOrderRefMapper;
import com.aicloud.module.pay.biz.model.AppPayOrderCreateRequest;
import com.aicloud.module.pay.biz.model.CreatePayOrderRequest;
import com.aicloud.module.pay.biz.model.PageResponse;
import com.aicloud.module.pay.biz.model.PayChannelSaveRequest;
import com.aicloud.module.pay.biz.model.PayRefundCreateRequest;
import com.aicloud.module.pay.biz.model.UpdatePayOrderStatusRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class PayOrderService {

    private static final String PAY_STATUS_WAITING = "WAITING";
    private static final String PAY_STATUS_SUCCESS = "SUCCESS";
    private static final String TRADE_STATUS_PAID = "PAID";
    private static final String TRADE_STATUS_PAYING = "PAYING";
    private static final String TRADE_STATUS_REFUNDED = "REFUNDED";
    private static final String PAY_ORDER_NOT_FOUND = "支付单不存在";
    private static final String PAY_CHANNEL_NOT_FOUND = "支付渠道不存在";
    private static final String PAY_REFUND_NOT_FOUND = "退款单不存在";
    private static final String TRADE_ORDER_NOT_FOUND = "关联订单不存在";

    private final PayOrderMapper payOrderMapper;
    private final TradeOrderRefMapper tradeOrderRefMapper;
    private final PayChannelMapper payChannelMapper;
    private final PayRefundMapper payRefundMapper;

    public PayOrderService(PayOrderMapper payOrderMapper, TradeOrderRefMapper tradeOrderRefMapper,
            PayChannelMapper payChannelMapper, PayRefundMapper payRefundMapper) {
        this.payOrderMapper = payOrderMapper;
        this.tradeOrderRefMapper = tradeOrderRefMapper;
        this.payChannelMapper = payChannelMapper;
        this.payRefundMapper = payRefundMapper;
    }

    public PageResponse<AiPayOrder> listAdmin(Long tenantId, String status, long pageNo, long pageSize) {
        Page<AiPayOrder> page = new Page<>(Math.max(pageNo, 1), Math.min(Math.max(pageSize, 1), 100));
        LambdaQueryWrapper<AiPayOrder> wrapper = new LambdaQueryWrapper<AiPayOrder>()
                .eq(AiPayOrder::getTenantId, tenantId)
                .eq(StringUtils.hasText(status), AiPayOrder::getStatus, status)
                .orderByDesc(AiPayOrder::getId);
        Page<AiPayOrder> result = payOrderMapper.selectPage(page, wrapper);
        PageResponse<AiPayOrder> data = new PageResponse<>();
        data.setTotal(result.getTotal());
        data.setPageNo(result.getCurrent());
        data.setPageSize(result.getSize());
        data.setList(result.getRecords());
        return data;
    }

    public List<AiPayOrder> listMember(Long tenantId, Long userId) {
        List<Long> tradeIds = tradeOrderRefMapper.selectList(new LambdaQueryWrapper<AiTradeOrderRef>()
                .eq(AiTradeOrderRef::getTenantId, tenantId)
                .eq(AiTradeOrderRef::getUserId, userId))
                .stream().map(AiTradeOrderRef::getId).collect(Collectors.toList());
        if (tradeIds.isEmpty()) {
            return List.of();
        }
        return payOrderMapper.selectList(new LambdaQueryWrapper<AiPayOrder>()
                .eq(AiPayOrder::getTenantId, tenantId)
                .in(AiPayOrder::getTradeOrderId, tradeIds)
                .orderByDesc(AiPayOrder::getId));
    }

    public AiPayOrder createAdmin(CreatePayOrderRequest body) {
        AiPayOrder order = new AiPayOrder();
        order.setTenantId(body.getTenantId());
        order.setTradeOrderId(body.getTradeOrderId());
        order.setPayOrderNo(StringUtils.hasText(body.getPayOrderNo()) ? body.getPayOrderNo() : defaultPayOrderNo());
        order.setChannel(body.getChannel());
        order.setAmount(body.getAmount());
        order.setStatus(StringUtils.hasText(body.getStatus()) ? body.getStatus() : PAY_STATUS_WAITING);
        order.setCreateTime(LocalDateTime.now());
        if (PAY_STATUS_SUCCESS.equalsIgnoreCase(order.getStatus())) {
            order.setSuccessTime(LocalDateTime.now());
            markTradeStatus(order.getTradeOrderId(), TRADE_STATUS_PAID);
        }
        payOrderMapper.insert(order);
        return order;
    }

    public AiPayOrder createMember(Long tenantId, Long userId, AppPayOrderCreateRequest body) {
        AiTradeOrderRef trade = getTrade(body.getTradeOrderId());
        if (!tenantId.equals(trade.getTenantId()) || !userId.equals(trade.getUserId())) {
            throw new IllegalArgumentException("无权为该订单发起支付");
        }
        AiPayOrder order = new AiPayOrder();
        order.setTenantId(tenantId);
        order.setTradeOrderId(trade.getId());
        order.setPayOrderNo(defaultPayOrderNo());
        order.setChannel(body.getChannel());
        order.setAmount(trade.getPayAmount());
        order.setStatus(PAY_STATUS_WAITING);
        order.setCreateTime(LocalDateTime.now());
        payOrderMapper.insert(order);
        trade.setStatus(TRADE_STATUS_PAYING);
        trade.setUpdateTime(LocalDateTime.now());
        tradeOrderRefMapper.updateById(trade);
        return order;
    }

    public AiPayOrder get(Long id) {
        AiPayOrder order = payOrderMapper.selectById(id);
        if (order == null) {
            throw new IllegalArgumentException(PAY_ORDER_NOT_FOUND);
        }
        return order;
    }

    public AiPayOrder updateStatus(UpdatePayOrderStatusRequest body) {
        AiPayOrder order = get(body.getId());
        order.setStatus(body.getStatus());
        if (PAY_STATUS_SUCCESS.equalsIgnoreCase(body.getStatus())) {
            order.setSuccessTime(LocalDateTime.now());
            markTradeStatus(order.getTradeOrderId(), TRADE_STATUS_PAID);
        }
        payOrderMapper.updateById(order);
        return order;
    }

    public AiPayOrder notifySuccess(Long payOrderId) {
        AiPayOrder order = get(payOrderId);
        order.setStatus(PAY_STATUS_SUCCESS);
        order.setSuccessTime(LocalDateTime.now());
        payOrderMapper.updateById(order);
        markTradeStatus(order.getTradeOrderId(), TRADE_STATUS_PAID);
        return order;
    }

    public List<AiPayChannel> channelList(Long tenantId, Integer status) {
        return payChannelMapper.selectList(new LambdaQueryWrapper<AiPayChannel>()
                .eq(AiPayChannel::getTenantId, tenantId)
                .eq(status != null, AiPayChannel::getStatus, status)
                .orderByDesc(AiPayChannel::getId));
    }

    public AiPayChannel saveChannel(PayChannelSaveRequest body) {
        AiPayChannel channel = body.getId() == null ? new AiPayChannel() : getChannel(body.getId());
        channel.setTenantId(body.getTenantId());
        channel.setChannelCode(body.getChannelCode());
        channel.setChannelName(body.getChannelName());
        channel.setAppId(body.getAppId());
        channel.setMchId(body.getMchId());
        channel.setNotifyUrl(body.getNotifyUrl());
        channel.setStatus(body.getStatus() == null ? 1 : body.getStatus());
        channel.setRemark(body.getRemark());
        channel.setUpdateTime(LocalDateTime.now());
        if (channel.getId() == null) {
            channel.setCreateTime(LocalDateTime.now());
            payChannelMapper.insert(channel);
        } else {
            payChannelMapper.updateById(channel);
        }
        return channel;
    }

    public List<AiPayRefund> refundList(Long tenantId, String status) {
        return payRefundMapper.selectList(new LambdaQueryWrapper<AiPayRefund>()
                .eq(AiPayRefund::getTenantId, tenantId)
                .eq(StringUtils.hasText(status), AiPayRefund::getStatus, status)
                .orderByDesc(AiPayRefund::getId));
    }

    public AiPayRefund createRefund(Long tenantId, PayRefundCreateRequest body) {
        AiPayOrder payOrder = get(body.getPayOrderId());
        if (!tenantId.equals(payOrder.getTenantId())) {
            throw new IllegalArgumentException("租户不匹配");
        }
        if (!PAY_STATUS_SUCCESS.equals(payOrder.getStatus())) {
            throw new IllegalArgumentException("只有支付成功的订单可以退款");
        }
        if (body.getAmount().compareTo(payOrder.getAmount()) > 0) {
            throw new IllegalArgumentException("退款金额不能超过支付金额");
        }
        AiPayRefund refund = new AiPayRefund();
        refund.setTenantId(tenantId);
        refund.setPayOrderId(payOrder.getId());
        refund.setTradeOrderId(payOrder.getTradeOrderId());
        refund.setRefundNo(defaultRefundNo());
        refund.setChannel(payOrder.getChannel());
        refund.setAmount(body.getAmount());
        refund.setReason(body.getReason());
        refund.setStatus(PAY_STATUS_WAITING);
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        payRefundMapper.insert(refund);
        return refund;
    }

    public AiPayRefund notifyRefundSuccess(Long id) {
        AiPayRefund refund = getRefund(id);
        refund.setStatus(PAY_STATUS_SUCCESS);
        refund.setSuccessTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        payRefundMapper.updateById(refund);
        markTradeStatus(refund.getTradeOrderId(), TRADE_STATUS_REFUNDED);
        return refund;
    }

    private AiPayChannel getChannel(Long id) {
        AiPayChannel channel = payChannelMapper.selectById(id);
        if (channel == null) {
            throw new IllegalArgumentException(PAY_CHANNEL_NOT_FOUND);
        }
        return channel;
    }

    private AiPayRefund getRefund(Long id) {
        AiPayRefund refund = payRefundMapper.selectById(id);
        if (refund == null) {
            throw new IllegalArgumentException(PAY_REFUND_NOT_FOUND);
        }
        return refund;
    }

    private AiTradeOrderRef getTrade(Long id) {
        AiTradeOrderRef trade = tradeOrderRefMapper.selectById(id);
        if (trade == null) {
            throw new IllegalArgumentException(TRADE_ORDER_NOT_FOUND);
        }
        return trade;
    }

    private void markTradeStatus(Long tradeOrderId, String status) {
        AiTradeOrderRef trade = getTrade(tradeOrderId);
        trade.setStatus(status);
        trade.setUpdateTime(LocalDateTime.now());
        tradeOrderRefMapper.updateById(trade);
    }

    private String defaultPayOrderNo() {
        return "P" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String defaultRefundNo() {
        return "R" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
