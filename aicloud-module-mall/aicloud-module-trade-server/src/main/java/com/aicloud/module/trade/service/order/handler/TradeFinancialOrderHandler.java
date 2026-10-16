package com.aicloud.module.trade.service.order.handler;
import com.aicloud.module.fms.api.business.FmsBusinessBridge;
import com.aicloud.module.trade.dal.dataobject.order.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
@Component
public class TradeFinancialOrderHandler implements TradeOrderHandler {
    @Resource private FmsBusinessBridge finance;
    @Override public void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> items) {
        finance.record("MALL_PAYMENT", order.getId(), order.getNo(), BigDecimal.valueOf(order.getPayPrice(), 2), true, "商城订单收款");
    }
}
