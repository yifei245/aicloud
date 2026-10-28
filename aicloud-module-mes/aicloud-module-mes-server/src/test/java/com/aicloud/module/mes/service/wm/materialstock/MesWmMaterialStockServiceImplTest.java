package com.aicloud.module.mes.service.wm.materialstock;

import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.framework.test.core.ut.BaseDbUnitTest;
import com.aicloud.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockPageReqVO;
import com.aicloud.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import com.aicloud.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import com.aicloud.module.mes.dal.mysql.wm.materialstock.MesWmMaterialStockMapper;
import com.aicloud.module.mes.service.md.item.MesMdItemService;
import com.aicloud.module.mes.service.md.item.MesMdItemTypeService;
import com.aicloud.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import com.aicloud.module.mes.service.wm.warehouse.MesWmWarehouseService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import static com.aicloud.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * {@link MesWmMaterialStockServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
@Import(MesWmMaterialStockServiceImpl.class)
public class MesWmMaterialStockServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MesWmMaterialStockServiceImpl materialStockService;

    @Resource
    private MesWmMaterialStockMapper materialStockMapper;

    @MockitoBean
    private MesMdItemService itemService;
    @MockitoBean
    private MesMdItemTypeService itemTypeService;
    @MockitoBean
    private MesWmWarehouseAreaService areaService;
    @MockitoBean
    private MesWmWarehouseService warehouseService;
    @MockitoBean private com.aicloud.module.wms.api.integration.WmsStockApi wms;
    @MockitoBean private com.aicloud.module.wms.api.integration.WmsStockBridge bridge;
    @MockitoBean private com.aicloud.module.mes.service.md.unitmeasure.MesMdUnitMeasureService unitService;
    @MockitoBean private com.aicloud.module.mes.service.wm.batch.MesWmBatchService batchService;
    @MockitoBean private com.aicloud.module.mes.service.wm.warehouse.MesWmWarehouseLocationService locationService;

    @org.junit.jupiter.api.BeforeEach void wmsBalances() {
        when(wms.positions(org.mockito.ArgumentMatchers.anyList())).thenReturn(com.aicloud.framework.common.pojo.CommonResult.success(java.util.List.of()));
    }

    @Test void quantityComesFromWmsInsteadOfHistoricalMesBalance() {
        var stock = createMaterialStockPojo(o -> {}); materialStockMapper.insert(stock);
        when(wms.positions(java.util.List.of(stock.getId()))).thenReturn(com.aicloud.framework.common.pojo.CommonResult.success(java.util.List.of(
                new com.aicloud.module.wms.api.integration.WmsStockApi.PositionBalance().setStockId(stock.getId())
                        .setQuantity(new BigDecimal("7")).setReservedQuantity(new BigDecimal("2")).setAvailableQuantity(new BigDecimal("5")))));
        var actual = materialStockService.getMaterialStock(stock.getId());
        assertEquals(new BigDecimal("7"), actual.getQuantity());
        assertEquals(new BigDecimal("5"), actual.getAvailableQuantity());
        org.junit.jupiter.api.Assertions.assertThrows(com.aicloud.framework.common.exception.ServiceException.class, () -> materialStockService.validateSelectedStock(
                stock.getId(), stock.getItemId(), stock.getBatchId(), stock.getBatchCode(), stock.getWarehouseId(), stock.getLocationId(), stock.getAreaId(), new BigDecimal("6")));
        assertEquals(0, new BigDecimal("10").compareTo(materialStockMapper.selectById(stock.getId()).getQuantity()));
    }

    @Test void historicalQuantityCannotBeUsedWithoutWmsAllocation() {
        var stock = createMaterialStockPojo(o -> {}); materialStockMapper.insert(stock);
        assertEquals(BigDecimal.ZERO, materialStockService.getMaterialStock(stock.getId()).getQuantity());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> materialStockService.updateMaterialStockQuantity(stock.getId(), BigDecimal.ONE, false));
    }

    @Test
    public void testGetMaterialStockPage_virtualFilterOnly() {
        // 准备数据：虚拟仓库存 + 普通仓库存
        Long virtualWarehouseId = 100L;
        Long normalWarehouseId = 200L;
        MesWmMaterialStockDO virtualStock = createMaterialStockPojo(o -> o.setWarehouseId(virtualWarehouseId));
        MesWmMaterialStockDO normalStock = createMaterialStockPojo(o -> o.setWarehouseId(normalWarehouseId));
        materialStockMapper.insert(virtualStock);
        materialStockMapper.insert(normalStock);
        mockVirtualWarehouse(virtualWarehouseId);

        // 调用：只看虚拟仓
        MesWmMaterialStockPageReqVO reqVO = new MesWmMaterialStockPageReqVO();
        reqVO.setVirtualFilter(MesWmMaterialStockPageReqVO.VIRTUAL_FILTER_ONLY);
        PageResult<MesWmMaterialStockDO> result = materialStockService.getMaterialStockPage(reqVO);

        // 断言：只返回虚拟仓库存，且 total 与列表一致
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(virtualStock.getId(), result.getList().get(0).getId());
    }

    @Test
    public void testGetMaterialStockPage_virtualFilterExclude() {
        // 准备数据：虚拟仓库存 + 普通仓库存
        Long virtualWarehouseId = 100L;
        Long normalWarehouseId = 200L;
        MesWmMaterialStockDO virtualStock = createMaterialStockPojo(o -> o.setWarehouseId(virtualWarehouseId));
        MesWmMaterialStockDO normalStock = createMaterialStockPojo(o -> o.setWarehouseId(normalWarehouseId));
        materialStockMapper.insert(virtualStock);
        materialStockMapper.insert(normalStock);
        mockVirtualWarehouse(virtualWarehouseId);

        // 调用：排除虚拟仓
        MesWmMaterialStockPageReqVO reqVO = new MesWmMaterialStockPageReqVO();
        reqVO.setVirtualFilter(MesWmMaterialStockPageReqVO.VIRTUAL_FILTER_EXCLUDE);
        PageResult<MesWmMaterialStockDO> result = materialStockService.getMaterialStockPage(reqVO);

        // 断言：只返回普通仓库存，且 total 与列表一致
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        assertEquals(normalStock.getId(), result.getList().get(0).getId());
    }

    private MesWmMaterialStockDO createMaterialStockPojo(Consumer<MesWmMaterialStockDO> consumer) {
        return randomPojo(MesWmMaterialStockDO.class, o -> {
            o.setQuantity(new BigDecimal("10.00"));
            o.setReceiptTime(LocalDateTime.of(2026, 1, 1, 0, 0));
            o.setFrozen(false);
            o.setDeleted(false);
            consumer.accept(o);
        });
    }

    private void mockVirtualWarehouse(Long virtualWarehouseId) {
        MesWmWarehouseDO virtualWarehouse = new MesWmWarehouseDO();
        virtualWarehouse.setId(virtualWarehouseId);
        when(warehouseService.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE))
                .thenReturn(virtualWarehouse);
    }

}
