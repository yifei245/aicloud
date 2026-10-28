package com.aicloud.module.mes.service.wm.materialstock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.framework.common.util.collection.SetUtils;
import com.aicloud.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockFreezeReqVO;
import com.aicloud.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockListReqVO;
import com.aicloud.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockPageReqVO;
import com.aicloud.module.mes.dal.dataobject.md.item.MesMdItemDO;
import com.aicloud.module.mes.dal.dataobject.md.item.MesMdItemTypeDO;
import com.aicloud.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import com.aicloud.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import com.aicloud.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import com.aicloud.module.mes.dal.mysql.wm.materialstock.MesWmMaterialStockMapper;
import com.aicloud.module.mes.service.md.item.MesMdItemService;
import com.aicloud.module.mes.service.md.item.MesMdItemTypeService;
import com.aicloud.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import com.aicloud.module.mes.service.wm.warehouse.MesWmWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.aicloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.aicloud.framework.common.util.collection.CollectionUtils.convertSet;
import static com.aicloud.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 库存台账 Service 实现类
 */
@Service
@Validated
public class MesWmMaterialStockServiceImpl implements MesWmMaterialStockService {

    @Resource
    private MesWmMaterialStockMapper materialStockMapper;

    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdItemTypeService itemTypeService;
    @Resource
    private MesWmWarehouseAreaService areaService;
    @Resource
    @Lazy
    private MesWmWarehouseService warehouseService;
    @Resource private com.aicloud.module.wms.api.integration.WmsStockApi wms;
    @Resource private com.aicloud.module.wms.api.integration.WmsStockBridge stockBridge;
    @Resource private com.aicloud.module.mes.service.md.unitmeasure.MesMdUnitMeasureService unitService;
    @Resource private com.aicloud.module.mes.service.wm.batch.MesWmBatchService batchService;
    @Resource @Lazy private com.aicloud.module.mes.service.wm.warehouse.MesWmWarehouseLocationService locationService;
    @Resource private com.aicloud.module.mes.dal.mysql.wm.warehouse.MesWmWarehouseAreaMapper areaMapper;

    @Override
    public MesWmMaterialStockDO getMaterialStock(Long id) {
        var stock = materialStockMapper.selectById(id);
        if (stock != null) hydrate(List.of(stock));
        return stock;
    }

    @Override
    public List<MesWmMaterialStockDO> getMaterialStockListByAreaId(Long areaId) {
        var rows = materialStockMapper.selectList(MesWmMaterialStockDO::getAreaId, areaId);
        hydrate(rows); return rows;
    }

    public MesWmMaterialStockDO validateMaterialStockExists(Long id) {
        MesWmMaterialStockDO stock = getMaterialStock(id);
        if (stock == null) {
            throw exception(WM_MATERIAL_STOCK_NOT_EXISTS);
        }
        return stock;
    }

    @Override
    public PageResult<MesWmMaterialStockDO> getMaterialStockPage(MesWmMaterialStockPageReqVO pageReqVO) {
        // 1.1 解析 itemTypeId：包含子分类
        Set<Long> itemTypeIds = null;
        if (pageReqVO.getItemTypeId() != null) {
            itemTypeIds = new HashSet<>();
            itemTypeIds.add(pageReqVO.getItemTypeId());
            List<MesMdItemTypeDO> children = itemTypeService.getItemTypeChildrenList(pageReqVO.getItemTypeId());
            itemTypeIds.addAll(convertSet(children, MesMdItemTypeDO::getId));
        }
        // 1.2 解析 itemId
        Set<Long> itemIds = null;
        if (pageReqVO.getItemId() != null) {
            itemIds = SetUtils.asSet(pageReqVO.getItemId());
        }
        // 1.3 解析 virtualFilter：转换为虚拟仓 warehouseId
        Long virtualWarehouseId = null;
        String virtualFilter = pageReqVO.getVirtualFilter();
        if (MesWmMaterialStockPageReqVO.VIRTUAL_FILTER_EXCLUDE.equals(virtualFilter)
                || MesWmMaterialStockPageReqVO.VIRTUAL_FILTER_ONLY.equals(virtualFilter)) {
            MesWmWarehouseDO virtualWarehouse = warehouseService.getWarehouseByCode(
                    MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE);
            Assert.notNull(virtualWarehouse, "虚拟仓库（WIP_VIRTUAL_WAREHOUSE）不存在");
            virtualWarehouseId = virtualWarehouse.getId();
        }

        // 2. 分页查询
        var page = materialStockMapper.selectPage(pageReqVO, itemTypeIds, itemIds, virtualWarehouseId);
        hydrate(page.getList()); return page;
    }

    @Override
    public void updateMaterialStockFrozen(MesWmMaterialStockFreezeReqVO updateReqVO) {
        // 校验存在
        validateMaterialStockExists(updateReqVO.getId());

        // 更新冻结状态
        materialStockMapper.updateById(new MesWmMaterialStockDO()
                .setId(updateReqVO.getId()).setFrozen(updateReqVO.getFrozen()));
    }

    @Override
    public void updateMaterialStockFrozen(Collection<Long> ids, boolean frozen) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        materialStockMapper.updateByIds(ids, new MesWmMaterialStockDO().setFrozen(frozen));
    }

    @Override
    public Long getMaterialStockCountByWarehouseId(Long warehouseId) {
        return materialStockMapper.selectCountByWarehouseId(warehouseId);
    }

    @Override
    public Long getMaterialStockCountByLocationId(Long locationId) {
        return materialStockMapper.selectCountByLocationId(locationId);
    }

    @Override
    public Long getMaterialStockCountByAreaId(Long areaId) {
        return materialStockMapper.selectCountByAreaId(areaId);
    }

    @Override
    public List<MesWmMaterialStockDO> getMaterialStockList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        var rows = materialStockMapper.selectListByIds(ids);
        hydrate(rows); return rows;
    }

    @Override
    public List<MesWmMaterialStockDO> getMaterialStockList(MesWmMaterialStockListReqVO reqVO) {
        var rows = materialStockMapper.selectList(reqVO);
        hydrate(rows); return rows;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public MesWmMaterialStockDO getOrCreateMaterialStock(Long itemId, Long warehouseId, Long locationId, Long areaId,
                                                         Long batchId, String batchCode, Long vendorId, LocalDateTime receiptTime) {
        // Serialize metadata creation per physical bin; remote zero-balance registration is harmless on rollback.
        if (areaMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MesWmWarehouseAreaDO>().eq("id", areaId).last("FOR UPDATE")) == null) throw new IllegalArgumentException("库位不存在");
        // 1. 查找已有库存记录
        MesWmMaterialStockDO stock = materialStockMapper.selectByCompositeKey(
                itemId, warehouseId, locationId, areaId, batchId);
        if (stock != null) {
            ensurePosition(stock);
            return stock;
        }

        // 2. 不存在则新建
        MesMdItemDO item = itemService.validateItemExists(itemId);
        MesWmMaterialStockDO newStock = MesWmMaterialStockDO.builder()
                .itemId(itemId).itemTypeId(item.getItemTypeId())
                .warehouseId(warehouseId).locationId(locationId).areaId(areaId)
                .batchId(batchId).batchCode(batchCode).vendorId(vendorId)
                .quantity(BigDecimal.ZERO) // 初始数量为 0，由 updateMaterialStockQuantity 更新
                .receiptTime(receiptTime != null ? receiptTime : LocalDateTime.now())
                .frozen(false)
                .build();
        materialStockMapper.insert(newStock);
        ensurePosition(newStock);
        return newStock;
    }

    @Override
    public void updateMaterialStockQuantity(Long id, BigDecimal quantity, boolean checkFlag) {
        throw new IllegalStateException("MES 数量由 WMS 库存事务管理，禁止直接修改本地数量");
    }

    @Override
    public void checkAreaMixingRule(Long areaId, Long itemId, Long batchId) {
        // 1.1 校验库位必须存在
        MesWmWarehouseAreaDO area = areaService.validateWarehouseAreaExists(areaId);

        // 2. 查询当前库位上已有的库存记录
        List<MesWmMaterialStockDO> stockList = getMaterialStockListByAreaId(areaId);
        stockList = stockList.stream().filter(stock -> stock.getQuantity().signum() > 0).toList();
        if (CollUtil.isEmpty(stockList)) {
            return;
        }
        // 2.1 检查物料混放
        if (Boolean.FALSE.equals(area.getAllowItemMixing()) && itemId != null) {
            if (CollUtil.anyMatch(stockList, item -> ObjUtil.notEqual(itemId, item.getItemId()))) {
                throw exception(WM_MATERIAL_STOCK_AREA_ITEM_MIXING_NOT_ALLOWED, area.getName());
            }
        }
        // 2.2 检查批次混放
        if (Boolean.FALSE.equals(area.getAllowBatchMixing()) && batchId != null) {
            if (CollUtil.anyMatch(stockList, item -> ObjUtil.notEqual(batchId, item.getBatchId()))) {
                throw exception(WM_MATERIAL_STOCK_AREA_BATCH_MIXING_NOT_ALLOWED, area.getName());
            }
        }
    }

    @Override
    public MesWmMaterialStockDO validateSelectedStock(Long materialStockId,
                                                      Long itemId,
                                                      Long batchId,
                                                      String batchCode,
                                                      Long warehouseId,
                                                      Long locationId,
                                                      Long areaId,
                                                      BigDecimal quantity) {
        // 1. 校验库存记录必须存在
        if (materialStockId == null) {
            throw exception(WM_MATERIAL_STOCK_REQUIRED);
        }
        MesWmMaterialStockDO stock = validateMaterialStockExists(materialStockId);
        // 2. 校验库存记录的物料、批次、仓库、库区、库位等信息与前端选择的一致，避免串单或越权提交
        if (ObjUtil.notEqual(stock.getItemId(), itemId)
                || ObjUtil.notEqual(stock.getBatchId(), batchId)
                || (batchCode != null && ObjUtil.notEqual(stock.getBatchCode(), batchCode))
                || ObjUtil.notEqual(stock.getWarehouseId(), warehouseId)
                || ObjUtil.notEqual(stock.getLocationId(), locationId)
                || ObjUtil.notEqual(stock.getAreaId(), areaId)) {
            throw exception(WM_MATERIAL_STOCK_SELECTION_MISMATCH);
        }
        // 3. 校验库存数量充足（如果前端传了 quantity，则必须保证库存数量 >= quantity）
        if (quantity != null && (quantity.signum() <= 0 || stock.getAvailableQuantity().compareTo(quantity) < 0)) {
            throw exception(WM_MATERIAL_STOCK_INSUFFICIENT);
        }
        return stock;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Long allocate(com.aicloud.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockAllocateReqVO request) {
        if (request.getBatchId() == null && Boolean.TRUE.equals(itemService.validateItemExists(request.getItemId()).getBatchFlag())) {
            if (request.getBatch() == null || !Objects.equals(request.getBatch().getItemId(), request.getItemId())) throw new IllegalArgumentException("批次管理物料必须选择已有批次或提供批次生成参数");
            var generated = batchService.getOrGenerateBatchCode(request.getBatch());
            request.setBatchId(Objects.requireNonNull(generated, "批次生成失败").getId());
        }
        var batch = request.getBatchId() == null ? null : batchService.getBatch(request.getBatchId());
        if (request.getBatchId() != null && batch == null) throw exception(WM_TRANSACTION_BATCH_NOT_EXISTS);
        String code = batch == null ? null : batch.getCode();
        checkAreaMixingRule(request.getAreaId(), request.getItemId(), request.getBatchId());
        var stock = getOrCreateMaterialStock(request.getItemId(), request.getWarehouseId(), request.getLocationId(), request.getAreaId(), request.getBatchId(), code, null, null);
        var warehouse = warehouseService.validateWarehouseExists(request.getWarehouseId());
        var location = locationService.validateWarehouseLocationExists(request.getLocationId());
        var area = areaService.validateWarehouseAreaExists(request.getAreaId());
        if (Boolean.TRUE.equals(warehouse.getFrozen()) || Boolean.TRUE.equals(location.getFrozen()) || Boolean.TRUE.equals(area.getFrozen()) || Boolean.TRUE.equals(stock.getFrozen())) throw new IllegalArgumentException("冻结的库存位置不能上架");
        var movement = new com.aicloud.module.wms.api.integration.WmsStockApi.Movement();
        movement.setBusinessKey("putaway:" + request.getBusinessKey()); movement.setDocumentId(stock.getId());
        movement.setDocumentNo("PUTAWAY-" + request.getBusinessKey()); movement.setProductId(request.getItemId()); movement.setWarehouseId(request.getWarehouseId());
        movement.setUnit(unitService.getUnitMeasure(itemService.validateItemExists(request.getItemId()).getUnitMeasureId()).getName());
        movement.setQuantity(request.getQuantity()); movement.setPositionId(stock.getId()); movement.setAllocationOnly(true);
        stockBridge.prepare(movement, true);
        return stock.getId();
    }

    private void ensurePosition(MesWmMaterialStockDO stock) {
        normalizeBatch(stock);
        var item = itemService.validateItemExists(stock.getItemId());
        var location = locationService.validateWarehouseLocationExists(stock.getLocationId());
        var area = areaService.validateWarehouseAreaExists(stock.getAreaId());
        if (!Objects.equals(location.getWarehouseId(), stock.getWarehouseId()) || !Objects.equals(area.getLocationId(), stock.getLocationId())) throw new IllegalArgumentException("仓库、库区、库位归属不一致");
        if (stock.getBatchId() != null) {
            var batch = batchService.getBatch(stock.getBatchId());
            if (batch == null || !Objects.equals(batch.getItemId(), stock.getItemId()) || !Objects.equals(batch.getCode(), stock.getBatchCode())) throw new IllegalArgumentException("批次与物料不匹配");
        } else if (Boolean.TRUE.equals(item.getBatchFlag()) || stock.getBatchCode() != null) throw exception(WM_TRANSACTION_BATCH_REQUIRED);
        var unit = unitService.getUnitMeasure(item.getUnitMeasureId());
        if (unit == null) throw new IllegalArgumentException("MES 物料基本单位不存在");
        wms.ensurePosition(new com.aicloud.module.wms.api.integration.WmsStockApi.Position().setStockId(stock.getId())
                .setProductId(stock.getItemId()).setWarehouseId(stock.getWarehouseId()).setLocationId(stock.getLocationId()).setAreaId(stock.getAreaId())
                .setBatchId(stock.getBatchId()).setBatchCode(stock.getBatchCode()).setUnit(unit.getName())).checkError();
    }

    private void hydrate(List<MesWmMaterialStockDO> rows) {
        for (int from = 0; from < rows.size(); from += 200) {
            var chunk = rows.subList(from, Math.min(from + 200, rows.size()));
            var balances = wms.positions(chunk.stream().map(MesWmMaterialStockDO::getId).toList()).getCheckedData();
            Map<Long, com.aicloud.module.wms.api.integration.WmsStockApi.PositionBalance> byId = new HashMap<>();
            balances.forEach(balance -> byId.put(balance.getStockId(), balance));
            for (var stock : chunk) {
                normalizeBatch(stock);
                var balance = byId.get(stock.getId());
                stock.setQuantity(balance == null ? BigDecimal.ZERO : balance.getQuantity());
                stock.setReservedQuantity(balance == null ? BigDecimal.ZERO : balance.getReservedQuantity());
                stock.setAvailableQuantity(balance == null ? BigDecimal.ZERO : balance.getAvailableQuantity());
            }
        }
    }
    private static void normalizeBatch(MesWmMaterialStockDO stock) {
        if (Long.valueOf(0).equals(stock.getBatchId())) stock.setBatchId(null);
        if (stock.getBatchCode() != null && stock.getBatchCode().isBlank()) stock.setBatchCode(null);
    }

}
