package com.aicloud.module.wms.api.integration;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.wms.service.inventory.WmsIntegrationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class WmsStockApiImpl implements WmsStockApi {
    @Resource private WmsIntegrationService service;
    @Override public CommonResult<Long> syncProduct(Product request) { return CommonResult.success(service.syncProduct(request)); }
    @Override public CommonResult<Long> syncWarehouse(Warehouse request) { return CommonResult.success(service.syncWarehouse(request)); }
    @Override public CommonResult<Balance> prepare(Movement request) { return CommonResult.success(service.prepare(request)); }
    @Override public CommonResult<Boolean> commit(Command request) { service.commit(request); return CommonResult.success(true); }
    @Override public CommonResult<Boolean> abort(Command request) { service.abort(request); return CommonResult.success(true); }
    @Override public CommonResult<Boolean> ensurePosition(Position request) { service.ensurePosition(request); return CommonResult.success(true); }
    @Override public CommonResult<java.util.List<PositionBalance>> positions(java.util.List<Long> ids) { return CommonResult.success(service.positions(ids)); }
    @Override public CommonResult<Balance> balance(String source, Long productId, Long warehouseId) { return CommonResult.success(service.balance(source, productId, warehouseId)); }
    @Override public CommonResult<com.aicloud.framework.common.pojo.PageResult<SourceBalance>> balances(String source, Long productId, Long warehouseId, Long inventoryId, Integer pageNo, Integer pageSize) {
        return CommonResult.success(service.balances(source, productId, warehouseId, inventoryId, pageNo, pageSize));
    }
}
