package com.aicloud.module.trade.service.order.handler;

import com.aicloud.module.wms.api.integration.WmsStockApi;
import com.aicloud.module.wms.api.integration.WmsStockBridge;
import com.aicloud.module.trade.dal.dataobject.order.*;
import com.aicloud.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import com.aicloud.module.trade.dal.mysql.order.TradeOrderItemMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

/** Orders reserve WMS stock; shipping consumes it and cancellation releases it. */
@Component
public class TradeProductSkuOrderHandler implements TradeOrderHandler {
    @Resource private WmsStockApi wms;
    @Resource private WmsStockBridge bridge;
    @Resource private TradeOrderItemMapper items;
    @Value("${aicloud.trade.fulfillment-warehouse-id:1}") private Long warehouseId;

    @Override public void afterOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        for (TradeOrderItemDO item : orderItems) {
            bridge.prepare(movement(order, item.getSkuId(), BigDecimal.valueOf(item.getCount()).negate(), key(item)), false);
        }
    }
    @Override public void afterCancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (order.getDeliveryTime() != null) return;
        for (TradeOrderItemDO item : filterOrderItemListByNoneAfterSale(orderItems)) bridge.finish(key(item), false);
    }
    @Override public void afterCancelOrderItem(TradeOrderDO order, TradeOrderItemDO item) {
        if (order.getDeliveryTime() == null) bridge.finish(key(item), false);
        // A refund without returned goods never increases physical inventory.
    }
    @Override public void afterDeliveryOrder(TradeOrderDO order) {
        for (TradeOrderItemDO item : filterOrderItemListByNoneAfterSale(items.selectListByOrderId(order.getId()))) bridge.finish(key(item), true);
    }
    public void returnReceived(TradeOrderDO order, AfterSaleDO afterSale) {
        if (order.getDeliveryTime() == null) throw new IllegalArgumentException("未发货订单不能进行实物退库");
        bridge.prepare(movement(order, afterSale.getSkuId(), BigDecimal.valueOf(afterSale.getCount()),
                "return-received:" + afterSale.getId()), true);
    }
    private WmsStockApi.Movement movement(TradeOrderDO order, Long sku, BigDecimal quantity, String key) {
        var balance = wms.balance("MALL", sku, warehouseId).getCheckedData();
        if (!balance.isMapped()) throw new IllegalArgumentException("商城 SKU 尚未关联 ERP 商品和 WMS 库存");
        var movement = new WmsStockApi.Movement();
        movement.setBusinessKey(key); movement.setProductId(sku); movement.setWarehouseId(warehouseId);
        movement.setQuantity(quantity); movement.setUnit(balance.getUnit());
        movement.setDocumentId(order.getId()); movement.setDocumentNo(order.getNo());
        return movement;
    }
    private String key(TradeOrderItemDO item) { return "order-item:" + item.getId(); }
}
