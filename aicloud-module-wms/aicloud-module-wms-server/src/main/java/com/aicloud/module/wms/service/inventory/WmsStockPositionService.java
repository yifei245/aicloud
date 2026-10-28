package com.aicloud.module.wms.service.inventory;

import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.wms.api.integration.WmsStockApi;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/** Batch/location allocations within the locked WMS warehouse balance. Never a second physical balance. */
@Service
public class WmsStockPositionService {
    @Resource private JdbcTemplate jdbc;

    public void ensure(WmsStockApi.Position request, Long sku, Long warehouse) {
        jdbc.update("INSERT INTO wms_stock_position(tenant_id,stock_id,sku_id,warehouse_id,location_id,area_id,batch_id,batch_code) VALUES(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE id=id",
                tenant(), request.getStockId(), sku, warehouse, request.getLocationId(), request.getAreaId(), request.getBatchId(), request.getBatchCode());
        var position = position(request.getStockId(), sku, warehouse);
        if (!sameId(position.get("location_id"), request.getLocationId()) || !sameId(position.get("area_id"), request.getAreaId())
                || !sameId(position.get("batch_id"), request.getBatchId()) || !Objects.equals(position.get("batch_code"), request.getBatchCode())) {
            throw new IllegalArgumentException("库存分配的库区、库位或批次不允许改绑");
        }
    }

    public List<WmsStockApi.PositionBalance> balances(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        if (ids.size() > 200 || ids.stream().anyMatch(id -> id == null || id <= 0)) throw new IllegalArgumentException("库存明细查询参数无效");
        List<Object> args = new ArrayList<>(); args.add(tenant()); args.addAll(ids);
        return jdbc.query("SELECT stock_id,quantity,reserved_quantity FROM wms_stock_position WHERE tenant_id=? AND stock_id IN ("
                + String.join(",", Collections.nCopies(ids.size(), "?")) + ")", (rs, n) -> new WmsStockApi.PositionBalance()
                .setStockId(rs.getLong("stock_id")).setQuantity(rs.getBigDecimal("quantity"))
                .setReservedQuantity(rs.getBigDecimal("reserved_quantity"))
                .setAvailableQuantity(rs.getBigDecimal("quantity").subtract(rs.getBigDecimal("reserved_quantity"))), args.toArray());
    }

    /** Called only while the warehouse quantity row is locked. */
    public void reserve(Long sku, Long warehouse, Long stockId, BigDecimal delta, boolean allocation, BigDecimal physical) {
        if (stockId == null) {
            if (allocation) throw new IllegalArgumentException("上架分配必须指定库存明细");
            return;
        }
        var position = position(stockId, sku, warehouse);
        if (allocation) {
            if (delta.signum() <= 0) throw new IllegalArgumentException("上架分配数量必须为正数");
            BigDecimal free = physical.subtract(allocated(sku, warehouse)).subtract(pendingAllocation(sku, warehouse));
            if (free.compareTo(delta) < 0) throw new IllegalArgumentException("未分配库存不足：" + free);
        } else if (delta.signum() < 0) {
            BigDecimal available = ((BigDecimal) position.get("quantity")).subtract((BigDecimal) position.get("reserved_quantity"));
            if (available.compareTo(delta.negate()) < 0) throw new IllegalArgumentException("指定批次/库位可用库存不足：" + available);
            jdbc.update("UPDATE wms_stock_position SET reserved_quantity=reserved_quantity+? WHERE tenant_id=? AND stock_id=?", delta.negate(), tenant(), stockId);
        }
    }

    public void release(Long sku, Long warehouse, Long stockId, BigDecimal quantity) {
        var position = position(stockId, sku, warehouse);
        if (((BigDecimal) position.get("reserved_quantity")).compareTo(quantity) < 0) throw new IllegalStateException("库存明细预占数量不一致");
        jdbc.update("UPDATE wms_stock_position SET reserved_quantity=reserved_quantity-? WHERE tenant_id=? AND stock_id=?", quantity, tenant(), stockId);
    }

    public void allocate(Long sku, Long warehouse, Long stockId, BigDecimal quantity) {
        position(stockId, sku, warehouse);
        jdbc.update("UPDATE wms_stock_position SET quantity=quantity+? WHERE tenant_id=? AND stock_id=?", quantity, tenant(), stockId);
    }

    /** All native, ERP, mall and MES physical changes pass here inside their warehouse transaction. */
    public void change(Long sku, Long warehouse, Long stockId, BigDecimal delta, BigDecimal before) {
        if (stockId != null) {
            var position = position(stockId, sku, warehouse);
            BigDecimal after = ((BigDecimal) position.get("quantity")).add(delta);
            if (after.compareTo((BigDecimal) position.get("reserved_quantity")) < 0) throw new IllegalArgumentException("指定批次/库位库存不足");
            jdbc.update("UPDATE wms_stock_position SET quantity=? WHERE tenant_id=? AND stock_id=?", after, tenant(), stockId);
            return;
        }
        if (delta.signum() >= 0) return; // Generic receipts remain unallocated until putaway.
        BigDecimal needed = delta.negate();
        BigDecimal unallocated = before.subtract(allocated(sku, warehouse)).subtract(pendingAllocation(sku, warehouse));
        if (unallocated.signum() < 0) throw new IllegalStateException("WMS 分配量与总量不一致");
        needed = needed.subtract(unallocated.min(needed));
        if (needed.signum() == 0) return;
        var rows = jdbc.queryForList("SELECT stock_id,quantity,reserved_quantity FROM wms_stock_position WHERE tenant_id=? AND sku_id=? AND warehouse_id=? ORDER BY id FOR UPDATE", tenant(), sku, warehouse);
        for (var row : rows) {
            BigDecimal take = ((BigDecimal) row.get("quantity")).subtract((BigDecimal) row.get("reserved_quantity")).min(needed);
            if (take.signum() > 0) {
                jdbc.update("UPDATE wms_stock_position SET quantity=quantity-? WHERE tenant_id=? AND stock_id=?", take, tenant(), row.get("stock_id"));
                needed = needed.subtract(take);
            }
            if (needed.signum() == 0) return;
        }
        throw new IllegalArgumentException("库存分配可用量不足");
    }

    private BigDecimal allocated(Long sku, Long warehouse) {
        // Locking reads see the latest committed rows under MySQL REPEATABLE READ.
        return jdbc.queryForList("SELECT quantity FROM wms_stock_position WHERE tenant_id=? AND sku_id=? AND warehouse_id=? ORDER BY id FOR UPDATE", BigDecimal.class, tenant(), sku, warehouse)
                .stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private BigDecimal pendingAllocation(Long sku, Long warehouse) {
        return jdbc.queryForObject("SELECT putaway_quantity FROM wms_inventory WHERE tenant_id=? AND sku_id=? AND warehouse_id=? AND deleted=0 FOR UPDATE", BigDecimal.class, tenant(), sku, warehouse);
    }
    private Map<String, Object> position(Long stockId, Long sku, Long warehouse) {
        var rows = jdbc.queryForList("SELECT * FROM wms_stock_position WHERE tenant_id=? AND stock_id=? AND sku_id=? AND warehouse_id=? FOR UPDATE", tenant(), stockId, sku, warehouse);
        if (rows.isEmpty()) throw new IllegalArgumentException("库存批次/库位未登记或与商品、仓库不匹配");
        return rows.getFirst();
    }
    private static boolean sameId(Object value, Long expected) { return value == null ? expected == null : expected != null && ((Number) value).longValue() == expected; }
    private static Long tenant() { return TenantContextHolder.getRequiredTenantId(); }
}
