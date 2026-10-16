package com.aicloud.module.wms.service.inventory;

import com.aicloud.framework.test.core.ut.BaseDbUnitTest;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.wms.api.integration.*;
import com.aicloud.module.wms.dal.mysql.inventory.WmsInventoryHistoryMapper;
import com.aicloud.module.wms.service.inventory.dto.WmsInventoryChangeReqDTO;
import com.aicloud.module.wms.service.md.item.*;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@Import({WmsIntegrationServiceTest.JdbcConfig.class, WmsIntegrationService.class, WmsInventoryServiceImpl.class, WmsInventoryHistoryServiceImpl.class})
class WmsIntegrationServiceTest extends BaseDbUnitTest {
    @Resource WmsIntegrationService service;
    @Resource WmsInventoryService inventory;
    @Resource JdbcTemplate jdbc;
    @Resource DataSource dataSource;
    @Resource PlatformTransactionManager transactions;
    @Resource WmsInventoryHistoryMapper histories;
    @MockitoBean ErpStockIntentApi erpIntent;
    @MockitoBean MesStockIntentApi mesIntent;
    @MockitoBean MallStockIntentApi mallIntent;
    @MockitoBean WmsItemSkuService skuService;
    @MockitoBean WmsItemService itemService;
    Long sku, warehouse;
    @org.springframework.boot.test.context.TestConfiguration
    static class JdbcConfig {
        @org.springframework.context.annotation.Bean JdbcTemplate jdbcTemplate(DataSource dataSource) { return new JdbcTemplate(dataSource); }
    }

    @BeforeEach void setup() throws Exception {
        TenantContextHolder.setTenantId(1L);
        Path root = Path.of("").toAbsolutePath();
        while (!Files.exists(root.resolve("sql/mysql/10-integration.sql"))) root = root.getParent();
        try (var connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ByteArrayResource(Files.readString(root.resolve("sql/mysql/10-integration.sql"))
                    .replace("USE aicloud;", "").getBytes(StandardCharsets.UTF_8)));
        }
        jdbc.update("DELETE FROM wms_integration_movement"); jdbc.update("DELETE FROM wms_integration_mapping");
        sku = service.syncProduct(new WmsStockApi.Product().setProductId(100L).setName("统一商品").setUnit("件").setEnabled(true));
        warehouse = service.syncWarehouse(new WmsStockApi.Warehouse().setWarehouseId(200L).setName("统一仓库"));
        service.bind("PRODUCT", "MALL", 300L, sku); service.bind("WAREHOUSE", "MALL", 1L, warehouse);
        var opening = movement("opening", "10"); service.prepare(opening); service.commit(opening);
    }
    @AfterEach void clearTenant() { TenantContextHolder.clear(); }
    @Test void reservationCancellationAndLatePrepareAreSafe() {
        var hold = movement("order-1", "-7");
        service.prepare(hold); service.prepare(hold);
        assertBalance("10", "7", "3");
        assertThrows(IllegalArgumentException.class, () -> service.prepare(movement("order-2", "-4")));
        assertBalance("10", "7", "3");
        service.abort(hold); service.abort(hold);
        assertBalance("10", "0", "10");
        assertThrows(IllegalArgumentException.class, () -> service.prepare(hold));
        var lostResponse = movement("cancel-before-prepare", "-2"); service.abort(lostResponse);
        assertThrows(IllegalArgumentException.class, () -> service.prepare(lostResponse));
        assertEquals(1, histories.selectList().size());
    }
    @Test void shippingConsumesOnceAndNativeOutboundCannotConsumeHolds() {
        var hold = movement("order-1", "-7"); service.prepare(hold);
        assertThrows(RuntimeException.class, () -> inventory.changeInventory(new WmsInventoryChangeReqDTO().setOrderId(1L).setOrderNo("manual")
                .setOrderType(2).setItems(List.of(new WmsInventoryChangeReqDTO.Item().setSkuId(sku).setWarehouseId(warehouse)
                        .setQuantity(new BigDecimal("-4")).setPrice(BigDecimal.ZERO)))));
        assertBalance("10", "7", "3");
        service.commit(hold); service.commit(hold); assertBalance("3", "0", "3");
        assertThrows(IllegalArgumentException.class, () -> service.abort(hold));
        assertEquals(2, histories.selectList().size());
    }
    @Test void changedPayloadUnitOrTenantCannotReuseAnotherMovement() {
        var hold = movement("order-1", "-2"); service.prepare(hold);
        assertThrows(IllegalArgumentException.class, () -> service.prepare(movement("order-1", "-3")));
        var wrongUnit = movement("unit-mismatch", "-1"); wrongUnit.setUnit("箱");
        assertThrows(IllegalArgumentException.class, () -> service.prepare(wrongUnit));
        TenantContextHolder.setTenantId(2L);
        assertFalse(service.balance("MALL", 300L, 1L).isMapped());
        assertThrows(IllegalArgumentException.class, () -> service.prepare(movement("order-1", "-1")));
        TenantContextHolder.setTenantId(1L); assertBalance("10", "2", "8");
    }
    @Test void targetTransactionRollbackRestoresInventoryAndMovement() {
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactions).executeWithoutResult(status -> {
            service.prepare(movement("rollback", "-5")); throw new IllegalStateException("abort source");
        }));
        assertBalance("10", "0", "10");
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM wms_integration_movement WHERE business_key='rollback'", Integer.class));
    }
    @Test void concurrentOrdersDoNotOversell() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                String key = "parallel-" + i;
                results.add(executor.submit(() -> {
                    TenantContextHolder.setTenantId(1L);
                    try { start.await(); service.prepare(movement(key, "-7")); return true; }
                    catch (IllegalArgumentException insufficient) { return false; }
                    finally { TenantContextHolder.clear(); }
                }));
            }
            start.countDown(); int successes = 0;
            for (Future<Boolean> result : results) if (result.get(20, TimeUnit.SECONDS)) successes++;
            assertEquals(1, successes); assertBalance("10", "7", "3");
        }
    }
    private WmsStockApi.Movement movement(String key, String quantity) {
        var request = new WmsStockApi.Movement();
        request.setSource("MALL"); request.setBusinessKey(key); request.setProductId(300L); request.setWarehouseId(1L);
        request.setUnit("件"); request.setQuantity(new BigDecimal(quantity)); request.setDocumentId(1L); request.setDocumentNo("TEST-1");
        return request;
    }
    private void assertBalance(String quantity, String reserved, String available) {
        var balance = service.balance("MALL", 300L, 1L);
        assertEquals(0, new BigDecimal(quantity).compareTo(balance.getQuantity()));
        assertEquals(0, new BigDecimal(reserved).compareTo(balance.getReservedQuantity()));
        assertEquals(0, new BigDecimal(available).compareTo(balance.getAvailableQuantity()));
    }
}
