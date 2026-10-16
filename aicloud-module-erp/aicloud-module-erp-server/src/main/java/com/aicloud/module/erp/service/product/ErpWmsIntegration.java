package com.aicloud.module.erp.service.product;

import com.aicloud.framework.common.enums.CommonStatusEnum;
import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.mybatis.core.integration.*;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.framework.tenant.core.util.TenantUtils;
import com.aicloud.module.erp.dal.dataobject.product.ErpProductDO;
import com.aicloud.module.erp.dal.mysql.product.*;
import com.aicloud.module.erp.dal.mysql.stock.ErpWarehouseMapper;
import com.aicloud.module.wms.api.integration.WmsStockApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ErpWmsIntegration implements IntegrationEventHandler {
    @Resource private ErpProductMapper productMapper;
    @Resource private ErpProductUnitMapper unitMapper;
    @Resource private ErpProductCategoryMapper categoryMapper;
    @Resource private ErpWarehouseMapper warehouseMapper;
    @Resource private WmsStockApi api;
    @Resource private IntegrationOutboxService outbox;

    public void validateMaster(ErpProductDO product) {
        if (categoryMapper.selectById(product.getCategoryId()) == null) throw new IllegalArgumentException("ERP 商品分类不存在");
        if (unitMapper.selectById(product.getUnitId()) == null) throw new IllegalArgumentException("ERP 基本单位不存在");
    }
    public WmsStockApi.Product snapshot(ErpProductDO product) {
        var unit = unitMapper.selectById(product.getUnitId());
        if (unit == null) throw new IllegalArgumentException("ERP 基本单位不存在");
        return new WmsStockApi.Product().setProductId(product.getId()).setName(product.getName()).setUnit(unit.getName())
                .setBarCode(product.getBarCode()).setEnabled(CommonStatusEnum.isEnable(product.getStatus()))
                .setPurchasePrice(product.getPurchasePrice()).setSalePrice(product.getSalePrice());
    }
    public void changed(ErpProductDO product) {
        outbox.enqueue(TenantContextHolder.getRequiredTenantId(), eventType(), "product-master:" + UUID.randomUUID(),
                new Change().setProduct(snapshot(product)));
    }
    public WmsStockApi.Product ensureProduct(Long id) {
        ErpProductDO product = productMapper.selectById(id);
        if (product == null || !CommonStatusEnum.isEnable(product.getStatus())) throw new IllegalArgumentException("ERP 商品不存在或已停用");
        WmsStockApi.Product snapshot = snapshot(product);
        api.syncProduct(snapshot).checkError();
        return snapshot;
    }
    public void ensureWarehouse(Long id) {
        var warehouse = warehouseMapper.selectById(id);
        if (warehouse == null || !CommonStatusEnum.isEnable(warehouse.getStatus())) throw new IllegalArgumentException("ERP 仓库不存在或已停用");
        api.syncWarehouse(new WmsStockApi.Warehouse().setWarehouseId(id).setName(warehouse.getName())).checkError();
    }
    @Override public String eventType() { return "ERP_MASTER_PRODUCT"; }
    @Override public void handle(IntegrationEvent event) {
        TenantUtils.execute(event.getTenantId(), () -> {
            Change change = JsonUtils.parseObject(event.getPayload(), Change.class);
            ErpProductDO current = productMapper.selectById(change.getProduct().getProductId());
            // Every retry reads the current master, so a delayed old event cannot restore stale data.
            WmsStockApi.Product product = current == null ? change.getProduct().setEnabled(false) : snapshot(current);
            api.syncProduct(product).checkError();
        });
    }
    @lombok.Data public static class Change { private WmsStockApi.Product product; }
}
