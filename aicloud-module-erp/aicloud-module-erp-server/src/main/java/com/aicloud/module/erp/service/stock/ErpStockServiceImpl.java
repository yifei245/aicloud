package com.aicloud.module.erp.service.stock;

import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.module.erp.controller.admin.stock.vo.stock.ErpStockPageReqVO;
import com.aicloud.module.erp.dal.dataobject.stock.ErpStockDO;
import com.aicloud.module.wms.api.integration.WmsStockApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.math.BigDecimal;

/** ERP stock views read WMS; erp_stock is retained only as historical data. */
@Service
@Validated
public class ErpStockServiceImpl implements ErpStockService {
    @Resource private WmsStockApi wms;
    @Override public ErpStockDO getStock(Long id) {
        var rows = wms.balances("ERP", null, null, id, 1, 1).getCheckedData().getList();
        return rows.isEmpty() ? null : convert(rows.getFirst());
    }
    @Override public ErpStockDO getStock(Long productId, Long warehouseId) {
        var rows = wms.balances("ERP", productId, warehouseId, null, 1, 1).getCheckedData().getList();
        return rows.isEmpty() ? new ErpStockDO().setProductId(productId).setWarehouseId(warehouseId).setCount(BigDecimal.ZERO) : convert(rows.getFirst());
    }
    @Override public BigDecimal getStockCount(Long productId) { return wms.balance("ERP", productId, null).getCheckedData().getAvailableQuantity(); }
    @Override public PageResult<ErpStockDO> getStockPage(ErpStockPageReqVO request) {
        var page = wms.balances("ERP", request.getProductId(), request.getWarehouseId(), null, request.getPageNo(), request.getPageSize()).getCheckedData();
        return new PageResult<>(page.getList().stream().map(this::convert).toList(), page.getTotal());
    }
    @Override public BigDecimal updateStockCountIncrement(Long productId, Long warehouseId, BigDecimal count) {
        throw new IllegalStateException("请经 ERP 库存流水提交 WMS 库存事务");
    }
    private ErpStockDO convert(WmsStockApi.SourceBalance balance) {
        return new ErpStockDO().setId(balance.getInventoryId()).setProductId(balance.getProductId())
                .setWarehouseId(balance.getSourceWarehouseId()).setCount(balance.getAvailableQuantity());
    }
}
