package com.aicloud.module.wms.service.inventory;

import cn.hutool.crypto.digest.DigestUtil;
import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.framework.tenant.core.util.TenantUtils;
import com.aicloud.module.wms.api.integration.*;
import com.aicloud.module.wms.dal.dataobject.md.item.*;
import com.aicloud.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import com.aicloud.module.wms.dal.mysql.md.item.*;
import com.aicloud.module.wms.dal.mysql.md.warehouse.WmsWarehouseMapper;
import com.aicloud.module.wms.service.inventory.dto.WmsInventoryChangeReqDTO;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.*;

@Service
public class WmsIntegrationService {
    @Resource private JdbcTemplate jdbc;
    @Resource private WmsItemMapper itemMapper;
    @Resource private WmsItemSkuMapper skuMapper;
    @Resource private WmsWarehouseMapper warehouseMapper;
    @Resource private WmsInventoryService inventoryService;
    @Resource private WmsStockPositionService positions;
    @Resource private ErpStockIntentApi erpIntent;
    @Resource private MesStockIntentApi mesIntent;
    @Resource private MallStockIntentApi mallIntent;
    @Resource private PlatformTransactionManager transactions;

    @Transactional(rollbackFor = Exception.class)
    public Long syncProduct(WmsStockApi.Product request) {
        Long tenant = tenant();
        Map<String, Object> mapping = lockMapping("PRODUCT", "ERP", request.getProductId());
        long skuId = ((Number) mapping.get("target_id")).longValue();
        WmsItemDO item;
        WmsItemSkuDO sku;
        if (skuId == 0) {
            item = new WmsItemDO().setCode("ERP-" + request.getProductId()).setName(request.getName())
                    .setUnit(request.getUnit()).setCategoryId(0L).setBrandId(0L);
            itemMapper.insert(item);
            sku = new WmsItemSkuDO().setCode("ERP-" + request.getProductId()).setItemId(item.getId()).setName(request.getName());
            skuMapper.insert(sku);
        } else {
            sku = Objects.requireNonNull(skuMapper.selectById(skuId), "映射的 WMS SKU 已不存在");
            item = Objects.requireNonNull(itemMapper.selectById(sku.getItemId()), "映射的 WMS 商品已不存在");
            if (!request.getUnit().equals(item.getUnit())) {
                Integer used = jdbc.queryForObject("SELECT COUNT(*) FROM wms_inventory_history WHERE tenant_id=? AND sku_id=?", Integer.class, tenant, skuId);
                if (used != null && used > 0) throw new IllegalArgumentException("商品已有库存流水，不能更改基本计量单位");
            }
        }
        itemMapper.updateById(new WmsItemDO().setId(item.getId()).setName(request.getName()).setUnit(request.getUnit()));
        skuMapper.updateById(new WmsItemSkuDO().setId(sku.getId()).setName(request.getName()).setBarCode(request.getBarCode())
                .setCostPrice(request.getPurchasePrice()).setSellingPrice(request.getSalePrice()));
        jdbc.update("UPDATE wms_integration_mapping SET target_id=?,enabled=? WHERE id=?", sku.getId(), request.getEnabled(), mapping.get("id"));
        return sku.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long syncWarehouse(WmsStockApi.Warehouse request) {
        Map<String, Object> mapping = lockMapping("WAREHOUSE", "ERP", request.getWarehouseId());
        long id = ((Number) mapping.get("target_id")).longValue();
        if (id == 0) {
            WmsWarehouseDO warehouse = new WmsWarehouseDO().setCode("ERP-" + request.getWarehouseId()).setName(request.getName()).setSort(0);
            warehouseMapper.insert(warehouse);
            id = warehouse.getId();
            jdbc.update("UPDATE wms_integration_mapping SET target_id=? WHERE id=?", id, mapping.get("id"));
        } else if (warehouseMapper.selectById(id) == null) throw new IllegalArgumentException("映射的 WMS 仓库已不存在");
        return id;
    }

    /** Only explicit binding is allowed; names or equal numeric IDs never imply a relationship. */
    @Transactional(rollbackFor = Exception.class)
    public void bind(String kind, String source, Long sourceId, Long targetId) {
        validateSource(source);
        if (!Set.of("PRODUCT", "WAREHOUSE").contains(kind) || sourceId == null || sourceId <= 0 || targetId == null || targetId <= 0) {
            throw new IllegalArgumentException("映射参数无效");
        }
        if ("PRODUCT".equals(kind)) {
            if (skuMapper.selectById(targetId) == null) throw new IllegalArgumentException("WMS SKU 不存在");
            Integer masters = jdbc.queryForObject("SELECT COUNT(*) FROM wms_integration_mapping WHERE tenant_id=? AND kind='PRODUCT' AND source='ERP' AND target_id=?",
                    Integer.class, tenant(), targetId);
            if (masters == null || masters == 0) throw new IllegalArgumentException("先关联 ERP 商品主档，再关联渠道或生产物料");
        } else if (warehouseMapper.selectById(targetId) == null) throw new IllegalArgumentException("WMS 仓库不存在");
        Map<String, Object> mapping = lockMapping(kind, source, sourceId);
        long previous = ((Number) mapping.get("target_id")).longValue();
        if (previous != 0 && previous != targetId) throw new IllegalArgumentException("已有映射不能直接改绑，请先处理关联业务");
        jdbc.update("UPDATE wms_integration_mapping SET target_id=? WHERE id=?", targetId, mapping.get("id"));
    }

    public List<Map<String, Object>> mappings(String kind, String source) {
        validateSource(source);
        return jdbc.queryForList("SELECT source_id,target_id,enabled FROM wms_integration_mapping WHERE tenant_id=? AND kind=? AND source=? ORDER BY source_id",
                tenant(), kind, source);
    }

    @Transactional(rollbackFor = Exception.class)
    public WmsStockApi.Balance prepare(WmsStockApi.Movement request) {
        validateSource(request.getSource());
        boolean allocation = Boolean.TRUE.equals(request.getAllocationOnly());
        if ("MES".equals(request.getSource()) && request.getPositionId() == null) throw new IllegalArgumentException("MES 库存事务必须指定批次/库位明细");
        if (!"MES".equals(request.getSource()) && (request.getPositionId() != null || allocation)) throw new IllegalArgumentException("只有 MES 可以指定生产库存明细");
        if (request.getQuantity().signum() == 0) throw new IllegalArgumentException("库存变更数量不能为零");
        Long tenant = tenant();
        String hash = DigestUtil.sha256Hex(JsonUtils.toJsonString(request));
        jdbc.update("INSERT INTO wms_integration_movement(tenant_id,source,business_key,payload_hash,state) VALUES(?,?,?,?,'NEW') ON DUPLICATE KEY UPDATE id=id",
                tenant, request.getSource(), request.getBusinessKey(), hash);
        Map<String, Object> movement = lockMovement(request);
        if ("ABORTED".equals(movement.get("state"))) throw new IllegalArgumentException("此库存业务已撤销");
        if (!hash.equals(movement.get("payload_hash"))) throw new IllegalArgumentException("库存业务键对应不同明细");
        if (!"NEW".equals(movement.get("state"))) return balanceByTarget((Long) movement.get("sku_id"), (Long) movement.get("warehouse_id"));
        Long skuId = target("PRODUCT", request.getSource(), request.getProductId());
        Long warehouseId = target("WAREHOUSE", request.getSource(), request.getWarehouseId());
        WmsItemSkuDO sku = Objects.requireNonNull(skuMapper.selectById(skuId), "WMS SKU 不存在");
        WmsItemDO item = Objects.requireNonNull(itemMapper.selectById(sku.getItemId()), "WMS 商品不存在");
        if (!Objects.equals(item.getUnit(), request.getUnit())) throw new IllegalArgumentException("基本计量单位与 ERP 主档不一致，禁止自动换算");
        Integer active = jdbc.queryForObject("SELECT COUNT(*) FROM wms_integration_mapping WHERE tenant_id=? AND kind='PRODUCT' AND source='ERP' AND target_id=? AND enabled=1",
                Integer.class, tenant, skuId);
        if ((active == null || active == 0) && request.getQuantity().signum() < 0) throw new IllegalArgumentException("ERP 主档商品已停用或未映射");
        Map<String, Object> stock = lockStock(skuId, warehouseId);
        BigDecimal quantity = (BigDecimal) stock.get("quantity");
        BigDecimal reserved = (BigDecimal) stock.get("reserved_quantity");
        positions.reserve(skuId, warehouseId, request.getPositionId(), request.getQuantity(), allocation, quantity);
        if (request.getQuantity().signum() < 0 || allocation) {
            BigDecimal needed = request.getQuantity().abs();
            if (quantity.subtract(reserved).compareTo(needed) < 0) throw new IllegalArgumentException("WMS 可用库存不足：" + quantity.subtract(reserved));
            jdbc.update("UPDATE wms_inventory SET reserved_quantity=reserved_quantity+? WHERE id=? AND tenant_id=?", needed, stock.get("id"), tenant);
            if (allocation) jdbc.update("UPDATE wms_inventory SET putaway_quantity=putaway_quantity+? WHERE id=? AND tenant_id=?", needed, stock.get("id"), tenant);
        }
        jdbc.update("UPDATE wms_integration_movement SET state='PREPARED',sku_id=?,warehouse_id=?,quantity=?,price=?,document_id=?,document_no=?,position_id=?,allocation_only=? WHERE id=?",
                skuId, warehouseId, request.getQuantity(), request.getPrice(), request.getDocumentId(), request.getDocumentNo(), request.getPositionId(), allocation, movement.get("id"));
        return balanceByTarget(skuId, warehouseId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void commit(WmsStockApi.Command command) {
        Map<String, Object> movement = lockMovement(command);
        if ("COMMITTED".equals(movement.get("state"))) return;
        if (!"PREPARED".equals(movement.get("state"))) throw new IllegalArgumentException("库存业务未预占或已撤销");
        Long sku = ((Number) movement.get("sku_id")).longValue(), warehouse = ((Number) movement.get("warehouse_id")).longValue();
        BigDecimal delta = (BigDecimal) movement.get("quantity");
        Map<String, Object> stock = lockStock(sku, warehouse);
        Long position = movement.get("position_id") == null ? null : ((Number) movement.get("position_id")).longValue();
        boolean allocation = ((Number) movement.get("allocation_only")).intValue() != 0;
        if (delta.signum() < 0 || allocation) release(stock, delta.abs());
        if (allocation) jdbc.update("UPDATE wms_inventory SET putaway_quantity=putaway_quantity-? WHERE tenant_id=? AND id=? AND putaway_quantity>=?", delta, tenant(), stock.get("id"), delta);
        if (!allocation && delta.signum() < 0 && position != null) positions.release(sku, warehouse, position, delta.negate());
        BigDecimal price = movement.get("price") == null ? BigDecimal.ZERO : (BigDecimal) movement.get("price");
        if (allocation) positions.allocate(sku, warehouse, position, delta);
        else inventoryService.changeInventory(new WmsInventoryChangeReqDTO()
                .setOrderId(((Number) movement.get("document_id")).longValue()).setOrderNo((String) movement.get("document_no"))
                .setOrderType(switch (command.getSource()) { case "ERP" -> 90; case "MES" -> 91; default -> 92; })
                .setItems(List.of(new WmsInventoryChangeReqDTO.Item().setSkuId(sku).setWarehouseId(warehouse)
                        .setPositionId(position).setQuantity(delta).setPrice(price).setTotalPrice(price.multiply(delta)).setRemark(command.getSource() + ":" + command.getBusinessKey()))));
        jdbc.update("UPDATE wms_integration_movement SET state='COMMITTED',update_time=CURRENT_TIMESTAMP WHERE id=?", movement.get("id"));
    }

    @Transactional(rollbackFor = Exception.class)
    public void abort(WmsStockApi.Command command) {
        validateSource(command.getSource());
        // A tombstone also blocks an in-flight prepare whose HTTP response was lost.
        jdbc.update("INSERT INTO wms_integration_movement(tenant_id,source,business_key,state) VALUES(?,?,?,'ABORTED') ON DUPLICATE KEY UPDATE id=id",
                tenant(), command.getSource(), command.getBusinessKey());
        Map<String, Object> movement = lockMovement(command);
        if ("ABORTED".equals(movement.get("state"))) return;
        if ("COMMITTED".equals(movement.get("state"))) throw new IllegalArgumentException("实物已变更，须使用退库/冲回单据");
        BigDecimal delta = (BigDecimal) movement.get("quantity");
        boolean allocation = ((Number) movement.get("allocation_only")).intValue() != 0;
        if (delta != null && (delta.signum() < 0 || allocation)) {
            Long sku = ((Number) movement.get("sku_id")).longValue(), warehouse = ((Number) movement.get("warehouse_id")).longValue();
            var stock = lockStock(sku, warehouse);
            release(stock, delta.abs());
            if (allocation) jdbc.update("UPDATE wms_inventory SET putaway_quantity=putaway_quantity-? WHERE tenant_id=? AND id=? AND putaway_quantity>=?", delta, tenant(), stock.get("id"), delta);
            if (!allocation && movement.get("position_id") != null) positions.release(sku, warehouse, ((Number) movement.get("position_id")).longValue(), delta.negate());
        }
        jdbc.update("UPDATE wms_integration_movement SET state='ABORTED',update_time=CURRENT_TIMESTAMP WHERE id=?", movement.get("id"));
    }

    public WmsStockApi.Balance balance(String source, Long product, Long warehouse) {
        validateSource(source);
        if (mappings("PRODUCT", source).stream().noneMatch(row -> ((Number) row.get("source_id")).longValue() == product)
                || (warehouse != null && mappings("WAREHOUSE", source).stream().noneMatch(row -> ((Number) row.get("source_id")).longValue() == warehouse))) {
            return new WmsStockApi.Balance().setMapped(false).setQuantity(BigDecimal.ZERO).setReservedQuantity(BigDecimal.ZERO).setAvailableQuantity(BigDecimal.ZERO);
        }
        return balanceByTarget(target("PRODUCT", source, product), warehouse == null ? null : target("WAREHOUSE", source, warehouse));
    }
    @Transactional(rollbackFor = Exception.class)
    public void ensurePosition(WmsStockApi.Position request) {
        Long sku = target("PRODUCT", "MES", request.getProductId()), warehouse = target("WAREHOUSE", "MES", request.getWarehouseId());
        var itemSku = Objects.requireNonNull(skuMapper.selectById(sku), "映射的 WMS SKU 不存在");
        var item = Objects.requireNonNull(itemMapper.selectById(itemSku.getItemId()), "映射的 WMS 商品不存在");
        if (!Objects.equals(item.getUnit(), request.getUnit())) throw new IllegalArgumentException("MES 基本单位与 ERP 主档不一致");
        lockStock(sku, warehouse);
        positions.ensure(request, sku, warehouse);
    }
    public List<WmsStockApi.PositionBalance> positions(List<Long> ids) { return positions.balances(ids); }
    private WmsStockApi.Balance balanceByTarget(Long sku, Long warehouse) {
        String filter = warehouse == null ? "" : " AND warehouse_id=?";
        List<Object> args = new ArrayList<>(List.of(tenant(), sku));
        if (warehouse != null) args.add(warehouse);
        Map<String, Object> sums = jdbc.queryForMap("SELECT COALESCE(SUM(quantity),0) AS quantity,COALESCE(SUM(reserved_quantity),0) AS reserved FROM wms_inventory WHERE tenant_id=? AND sku_id=? AND deleted=0" + filter, args.toArray());
        BigDecimal quantity = (BigDecimal) sums.get("quantity"), reserved = (BigDecimal) sums.get("reserved");
        var itemSku = skuMapper.selectById(sku);
        if (itemSku == null) throw new IllegalArgumentException("映射的 WMS SKU 不存在");
        var item = itemMapper.selectById(itemSku.getItemId());
        return new WmsStockApi.Balance().setMapped(true).setUnit(item.getUnit()).setSkuId(sku).setWarehouseId(warehouse).setQuantity(quantity)
                .setReservedQuantity(reserved).setAvailableQuantity(quantity.subtract(reserved));
    }
    public com.aicloud.framework.common.pojo.PageResult<WmsStockApi.SourceBalance> balances(String source, Long product, Long warehouse, Long inventory, int pageNo, int pageSize) {
        validateSource(source);
        if (pageNo < 1 || (pageSize != -1 && (pageSize < 1 || pageSize > 100))) throw new IllegalArgumentException("分页参数无效");
        String from = " FROM wms_inventory s JOIN wms_integration_mapping p ON p.tenant_id=s.tenant_id AND p.kind='PRODUCT' AND p.source=? AND p.target_id=s.sku_id"
                + " JOIN wms_integration_mapping w ON w.tenant_id=s.tenant_id AND w.kind='WAREHOUSE' AND w.source=? AND w.target_id=s.warehouse_id"
                + " WHERE s.tenant_id=? AND s.deleted=0";
        List<Object> parameters = new ArrayList<>(List.of(source, source, tenant()));
        if (product != null) { from += " AND p.source_id=?"; parameters.add(product); }
        if (warehouse != null) { from += " AND w.source_id=?"; parameters.add(warehouse); }
        if (inventory != null) { from += " AND s.id=?"; parameters.add(inventory); }
        Long total = jdbc.queryForObject("SELECT COUNT(*)" + from, Long.class, parameters.toArray());
        String query = "SELECT s.id,s.sku_id,s.warehouse_id,s.quantity,s.reserved_quantity,p.source_id AS product_id,w.source_id AS source_warehouse_id" + from + " ORDER BY s.id";
        if (pageSize != -1) { query += " LIMIT ? OFFSET ?"; parameters.add(pageSize); parameters.add((long) (pageNo - 1) * pageSize); }
        var rows = jdbc.query(query, (rs, n) -> {
            var balance = new WmsStockApi.SourceBalance();
            balance.setInventoryId(rs.getLong("id")); balance.setProductId(rs.getLong("product_id")); balance.setSourceWarehouseId(rs.getLong("source_warehouse_id"));
            balance.setSkuId(rs.getLong("sku_id")); balance.setWarehouseId(rs.getLong("warehouse_id")); balance.setMapped(true);
            balance.setQuantity(rs.getBigDecimal("quantity")); balance.setReservedQuantity(rs.getBigDecimal("reserved_quantity"));
            balance.setAvailableQuantity(balance.getQuantity().subtract(balance.getReservedQuantity()));
            return balance;
        }, parameters.toArray());
        return new com.aicloud.framework.common.pojo.PageResult<>(rows, total);
    }
    private Map<String, Object> lockStock(Long sku, Long warehouse) {
        jdbc.update("INSERT INTO wms_inventory(tenant_id,sku_id,warehouse_id,quantity,reserved_quantity) VALUES(?,?,?,0,0) ON DUPLICATE KEY UPDATE id=id", tenant(), sku, warehouse);
        return jdbc.queryForMap("SELECT id,quantity,reserved_quantity FROM wms_inventory WHERE tenant_id=? AND sku_id=? AND warehouse_id=? AND deleted=0 FOR UPDATE", tenant(), sku, warehouse);
    }
    private void release(Map<String, Object> stock, BigDecimal quantity) {
        if (((BigDecimal) stock.get("reserved_quantity")).compareTo(quantity) < 0) throw new IllegalStateException("WMS 预占数量不一致");
        jdbc.update("UPDATE wms_inventory SET reserved_quantity=reserved_quantity-? WHERE tenant_id=? AND id=?", quantity, tenant(), stock.get("id"));
    }
    private Long target(String kind, String source, Long sourceId) {
        var ids = jdbc.queryForList("SELECT target_id FROM wms_integration_mapping WHERE tenant_id=? AND kind=? AND source=? AND source_id=? AND target_id>0", Long.class, tenant(), kind, source, sourceId);
        if (ids.isEmpty()) throw new IllegalArgumentException("缺少 WMS " + kind + " 映射：" + source + "/" + sourceId);
        return ids.getFirst();
    }
    private Map<String, Object> lockMapping(String kind, String source, Long sourceId) {
        jdbc.update("INSERT INTO wms_integration_mapping(tenant_id,kind,source,source_id,target_id) VALUES(?,?,?,?,0) ON DUPLICATE KEY UPDATE id=id", tenant(), kind, source, sourceId);
        return jdbc.queryForMap("SELECT id,target_id FROM wms_integration_mapping WHERE tenant_id=? AND kind=? AND source=? AND source_id=? FOR UPDATE", tenant(), kind, source, sourceId);
    }
    private Map<String, Object> lockMovement(WmsStockApi.Command command) {
        validateSource(command.getSource());
        var rows = jdbc.queryForList("SELECT * FROM wms_integration_movement WHERE tenant_id=? AND source=? AND business_key=? FOR UPDATE", tenant(), command.getSource(), command.getBusinessKey());
        if (rows.isEmpty()) throw new IllegalArgumentException("库存业务不存在");
        return rows.getFirst();
    }
    private static void validateSource(String source) { if (!Set.of("ERP", "MES", "MALL").contains(source)) throw new IllegalArgumentException("未知库存来源"); }
    private static Long tenant() { return TenantContextHolder.getRequiredTenantId(); }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    public void reconcile() {
        var rows = jdbc.queryForList("SELECT tenant_id,source,business_key FROM wms_integration_movement WHERE state='PREPARED' AND create_time<DATE_SUB(CURRENT_TIMESTAMP,INTERVAL 5 MINUTE) ORDER BY id LIMIT 100");
        for (Map<String, Object> row : rows) {
            try {
                TenantUtils.execute(((Number) row.get("tenant_id")).longValue(), () -> {
                    String source = (String) row.get("source"), key = (String) row.get("business_key");
                    StockIntentApi intent = switch (source) { case "ERP" -> erpIntent; case "MES" -> mesIntent; default -> mallIntent; };
                    String decision = intent.decision(key).getCheckedData();
                    new TransactionTemplate(transactions).executeWithoutResult(status -> {
                        WmsStockApi.Command command = new WmsStockApi.Command().setSource(source).setBusinessKey(key);
                        if ("COMMIT".equals(decision)) commit(command);
                        else if ("ABORT".equals(decision)) abort(command);
                        else if (!"RESERVED".equals(decision)) throw new IllegalArgumentException("未知库存源决策");
                    });
                });
            } catch (Exception failure) {
                // Unavailable source never implies cancellation. The retained row is retried on the next scan.
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("Stock reconciliation pending: source={}, key={}, failure={}",
                        row.get("source"), row.get("business_key"), failure.getClass().getSimpleName());
            }
        }
    }
}
