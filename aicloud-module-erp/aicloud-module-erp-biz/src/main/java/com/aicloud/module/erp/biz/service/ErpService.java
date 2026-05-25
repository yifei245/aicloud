package com.aicloud.module.erp.biz.service;

import com.aicloud.module.erp.biz.entity.AiErpInventory;
import com.aicloud.module.erp.biz.entity.AiErpStockCheck;
import com.aicloud.module.erp.biz.entity.AiErpStockRecord;
import com.aicloud.module.erp.biz.mapper.ErpInventoryMapper;
import com.aicloud.module.erp.biz.mapper.ErpStockCheckMapper;
import com.aicloud.module.erp.biz.mapper.ErpStockRecordMapper;
import com.aicloud.module.erp.biz.model.ErpInventoryAdjustRequest;
import com.aicloud.module.erp.biz.model.ErpStockCheckSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class ErpService {

    private final ErpInventoryMapper inventoryMapper;
    private final ErpStockRecordMapper recordMapper;
    private final ErpStockCheckMapper checkMapper;

    public ErpService(ErpInventoryMapper inventoryMapper, ErpStockRecordMapper recordMapper,
            ErpStockCheckMapper checkMapper) {
        this.inventoryMapper = inventoryMapper;
        this.recordMapper = recordMapper;
        this.checkMapper = checkMapper;
    }

    public List<AiErpInventory> inventoryList(Long tenantId, String warehouseCode, String skuCode) {
        return inventoryMapper.selectList(new LambdaQueryWrapper<AiErpInventory>()
                .eq(AiErpInventory::getTenantId, tenantId)
                .eq(StringUtils.hasText(warehouseCode), AiErpInventory::getWarehouseCode, warehouseCode)
                .eq(StringUtils.hasText(skuCode), AiErpInventory::getSkuCode, skuCode)
                .orderByDesc(AiErpInventory::getId));
    }

    public AiErpInventory getInventory(Long id) {
        AiErpInventory inventory = inventoryMapper.selectById(id);
        if (inventory == null) {
            throw new IllegalArgumentException("库存不存在");
        }
        return inventory;
    }

    public Map<String, Object> summary(Long tenantId, String warehouseCode) {
        List<AiErpInventory> list = inventoryList(tenantId, warehouseCode, null);
        Map<String, Object> data = new HashMap<>();
        data.put("tenantId", tenantId);
        data.put("warehouseCode", warehouseCode);
        data.put("skuCount", list.size());
        data.put("availableQty", list.stream().mapToInt(item -> value(item.getAvailableQty())).sum());
        data.put("lockedQty", list.stream().mapToInt(item -> value(item.getLockedQty())).sum());
        data.put("lowStockCount", list.stream().filter(item -> value(item.getAvailableQty()) <= value(item.getSafetyStock())).count());
        return data;
    }

    public List<AiErpInventory> lowStock(Long tenantId, String warehouseCode) {
        return inventoryList(tenantId, warehouseCode, null).stream()
                .filter(item -> value(item.getAvailableQty()) <= value(item.getSafetyStock()))
                .toList();
    }

    public AiErpInventory adjust(ErpInventoryAdjustRequest request) {
        AiErpInventory inventory = findInventory(request.getTenantId(), request.getSkuCode(), request.getWarehouseCode());
        if (inventory == null) {
            inventory = newInventory(request.getTenantId(), request.getSkuCode(), request.getSkuName(), request.getWarehouseCode());
        }
        int after = value(inventory.getAvailableQty()) + request.getQtyDelta();
        if (after < 0) {
            throw new IllegalArgumentException("库存不足");
        }
        inventory.setSkuName(request.getSkuName());
        inventory.setAvailableQty(after);
        inventory.setUpdateTime(LocalDateTime.now());
        saveInventory(inventory);
        createRecord(request.getTenantId(), request.getSkuCode(), request.getWarehouseCode(), request.getBizType(),
                request.getBizType() + "-" + shortUuid(), request.getQtyDelta(), after, request.getRemark(), request.getCreateBy());
        return inventory;
    }

    public AiErpInventory lock(Long tenantId, String skuCode, String warehouseCode, Integer qty, String bizNo, String createBy) {
        AiErpInventory inventory = requireInventory(tenantId, skuCode, warehouseCode);
        if (qty == null || qty <= 0) {
            throw new IllegalArgumentException("锁定数量必须大于 0");
        }
        if (value(inventory.getAvailableQty()) < qty) {
            throw new IllegalArgumentException("可用库存不足");
        }
        inventory.setAvailableQty(value(inventory.getAvailableQty()) - qty);
        inventory.setLockedQty(value(inventory.getLockedQty()) + qty);
        inventory.setUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(inventory);
        createRecord(tenantId, skuCode, warehouseCode, "LOCK", defaultBizNo("LOCK", bizNo), -qty,
                inventory.getAvailableQty(), "锁定库存", createBy);
        return inventory;
    }

    public AiErpInventory release(Long tenantId, String skuCode, String warehouseCode, Integer qty, String bizNo, String createBy) {
        AiErpInventory inventory = requireInventory(tenantId, skuCode, warehouseCode);
        if (qty == null || qty <= 0) {
            throw new IllegalArgumentException("释放数量必须大于 0");
        }
        if (value(inventory.getLockedQty()) < qty) {
            throw new IllegalArgumentException("锁定库存不足");
        }
        inventory.setAvailableQty(value(inventory.getAvailableQty()) + qty);
        inventory.setLockedQty(value(inventory.getLockedQty()) - qty);
        inventory.setUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(inventory);
        createRecord(tenantId, skuCode, warehouseCode, "RELEASE", defaultBizNo("REL", bizNo), qty,
                inventory.getAvailableQty(), "释放库存", createBy);
        return inventory;
    }

    public List<AiErpStockRecord> recordList(Long tenantId, String skuCode, String bizNo) {
        return recordMapper.selectList(new LambdaQueryWrapper<AiErpStockRecord>()
                .eq(AiErpStockRecord::getTenantId, tenantId)
                .eq(StringUtils.hasText(skuCode), AiErpStockRecord::getSkuCode, skuCode)
                .eq(StringUtils.hasText(bizNo), AiErpStockRecord::getBizNo, bizNo)
                .orderByDesc(AiErpStockRecord::getId));
    }

    public List<AiErpStockCheck> checkList(Long tenantId) {
        return checkMapper.selectList(new LambdaQueryWrapper<AiErpStockCheck>()
                .eq(AiErpStockCheck::getTenantId, tenantId)
                .orderByDesc(AiErpStockCheck::getId));
    }

    public AiErpStockCheck getCheck(Long id) {
        AiErpStockCheck check = checkMapper.selectById(id);
        if (check == null) {
            throw new IllegalArgumentException("盘点单不存在");
        }
        return check;
    }

    public AiErpStockCheck saveCheck(ErpStockCheckSaveRequest request) {
        AiErpStockCheck check = request.getId() == null ? new AiErpStockCheck() : getCheck(request.getId());
        check.setTenantId(request.getTenantId());
        check.setCheckNo(StringUtils.hasText(request.getCheckNo()) ? request.getCheckNo() : "CHK-" + shortUuid());
        check.setWarehouseCode(request.getWarehouseCode());
        check.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "DRAFT");
        check.setRemark(request.getRemark());
        check.setCreateBy(request.getCreateBy());
        check.setUpdateTime(LocalDateTime.now());
        if (check.getId() == null) {
            check.setCreateTime(LocalDateTime.now());
            checkMapper.insert(check);
        } else {
            checkMapper.updateById(check);
        }
        return check;
    }

    public AiErpStockCheck finishCheck(Long id) {
        AiErpStockCheck check = getCheck(id);
        check.setStatus("FINISHED");
        check.setUpdateTime(LocalDateTime.now());
        checkMapper.updateById(check);
        return check;
    }

    public void deleteCheck(Long id) {
        getCheck(id);
        checkMapper.deleteById(id);
    }

    private AiErpInventory requireInventory(Long tenantId, String skuCode, String warehouseCode) {
        AiErpInventory inventory = findInventory(tenantId, skuCode, warehouseCode);
        if (inventory == null) {
            throw new IllegalArgumentException("库存不存在");
        }
        return inventory;
    }

    private AiErpInventory findInventory(Long tenantId, String skuCode, String warehouseCode) {
        return inventoryMapper.selectOne(new LambdaQueryWrapper<AiErpInventory>()
                .eq(AiErpInventory::getTenantId, tenantId)
                .eq(AiErpInventory::getSkuCode, skuCode)
                .eq(AiErpInventory::getWarehouseCode, warehouseCode));
    }

    private AiErpInventory newInventory(Long tenantId, String skuCode, String skuName, String warehouseCode) {
        AiErpInventory inventory = new AiErpInventory();
        inventory.setTenantId(tenantId);
        inventory.setSkuCode(skuCode);
        inventory.setSkuName(skuName);
        inventory.setWarehouseCode(warehouseCode);
        inventory.setAvailableQty(0);
        inventory.setLockedQty(0);
        inventory.setSafetyStock(0);
        inventory.setUnitCost(BigDecimal.ZERO);
        inventory.setCreateTime(LocalDateTime.now());
        return inventory;
    }

    private void saveInventory(AiErpInventory inventory) {
        if (inventory.getId() == null) {
            inventoryMapper.insert(inventory);
        } else {
            inventoryMapper.updateById(inventory);
        }
    }

    private void createRecord(Long tenantId, String skuCode, String warehouseCode, String bizType, String bizNo,
            Integer qtyDelta, Integer qtyAfter, String remark, String createBy) {
        AiErpStockRecord record = new AiErpStockRecord();
        record.setTenantId(tenantId);
        record.setSkuCode(skuCode);
        record.setWarehouseCode(warehouseCode);
        record.setBizType(bizType);
        record.setBizNo(bizNo);
        record.setQtyDelta(qtyDelta);
        record.setQtyAfter(qtyAfter);
        record.setRemark(remark);
        record.setCreateBy(createBy);
        record.setCreateTime(LocalDateTime.now());
        recordMapper.insert(record);
    }

    private String defaultBizNo(String prefix, String bizNo) {
        return StringUtils.hasText(bizNo) ? bizNo : prefix + "-" + shortUuid();
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
