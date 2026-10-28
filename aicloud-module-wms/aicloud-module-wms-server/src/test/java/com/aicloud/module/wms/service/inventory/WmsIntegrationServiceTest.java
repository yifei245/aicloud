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

@Import({WmsIntegrationServiceTest.JdbcConfig.class, WmsIntegrationService.class, WmsStockPositionService.class, WmsInventoryServiceImpl.class, WmsInventoryHistoryServiceImpl.class})
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
            ScriptUtils.executeSqlScript(connection, new ByteArrayResource(java.util.regex.Pattern.compile("CREATE TABLE IF NOT EXISTS `wms_(?:integration_[^`]+|stock_position)`.*?;",java.util.regex.Pattern.DOTALL)
                    .matcher(Files.readString(root.resolve("sql/mysql/10-integration.sql"))).results()
                    .map(java.util.regex.MatchResult::group).collect(java.util.stream.Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8)));
        }
        jdbc.update("DELETE FROM wms_integration_movement"); jdbc.update("DELETE FROM wms_integration_mapping"); jdbc.update("DELETE FROM wms_stock_position");
        sku = service.syncProduct(new WmsStockApi.Product().setProductId(100L).setName("统一商品").setUnit("件").setEnabled(true));
        warehouse = service.syncWarehouse(new WmsStockApi.Warehouse().setWarehouseId(200L).setName("统一仓库"));
        service.bind("PRODUCT", "MALL", 300L, sku); service.bind("WAREHOUSE", "MALL", 1L, warehouse);
        var opening = movement("opening", "10"); service.prepare(opening); service.commit(opening);
        service.bind("PRODUCT", "MES", 400L, sku); service.bind("WAREHOUSE", "MES", 500L, warehouse);
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

    @Test void putawayReclassifiesWithoutCreatingStockAndCannotDoubleAllocate() {
        ensure(11L, 1L); ensure(12L, 2L);
        var putaway = mes("putaway-1", "8", 11L); putaway.setAllocationOnly(true);
        service.prepare(putaway); service.prepare(putaway); assertBalance("10", "8", "2");
        var other = mes("putaway-2", "3", 12L); other.setAllocationOnly(true);
        assertThrows(IllegalArgumentException.class, () -> service.prepare(other));
        service.commit(putaway); service.commit(putaway); assertBalance("10", "0", "10");
        assertPosition(11L, "8", "0");
        assertEquals(1, histories.selectList().size());
        assertThrows(IllegalArgumentException.class, () -> service.prepare(other));
    }

    @Test void sameBatchCannotUseAnotherBatchQuantityAndAbortReleasesBothHolds() {
        putaway(11L, 1L, "4"); putaway(12L, 2L, "6");
        var issue = mes("issue-1", "-3", 11L); service.prepare(issue);
        assertPosition(11L, "4", "3"); assertBalance("10", "3", "7");
        assertThrows(IllegalArgumentException.class, () -> service.prepare(mes("wrong-batch", "-2", 11L)));
        service.abort(issue); service.abort(issue); assertPosition(11L, "4", "0"); assertBalance("10", "0", "10");
        assertThrows(IllegalArgumentException.class, () -> service.prepare(issue));
    }

    @Test void genericOutboundUpdatesPositionsButProtectsReservedBatchAndPutaway() {
        putaway(11L, 1L, "6");
        var pending = mes("pending-putaway", "2", 12L); ensure(12L, 2L); pending.setAllocationOnly(true); service.prepare(pending);
        var held = mes("held-batch", "-3", 11L); service.prepare(held);
        var shipment = movement("shipment", "-5"); service.prepare(shipment); service.commit(shipment);
        assertBalance("5", "5", "0"); assertPosition(11L, "3", "3");
        service.commit(pending); assertBalance("5", "3", "2"); assertPosition(12L, "2", "0");
        service.commit(held); assertBalance("2", "0", "2"); assertPosition(11L, "0", "0");
    }

    @Test void physicalIssueAndReturnUpdateBothPositionAndTotalExactlyOnce() {
        putaway(11L, 1L, "8");
        var issue = mes("issue", "-3", 11L); service.prepare(issue); service.commit(issue); service.commit(issue);
        assertBalance("7", "0", "7"); assertPosition(11L, "5", "0");
        var returned = mes("return", "3", 11L); service.prepare(returned); service.commit(returned);
        assertBalance("10", "0", "10"); assertPosition(11L, "8", "0");
    }

    @Test void failedTargetTransactionRestoresAllocationAndReservation() {
        putaway(11L, 1L, "8");
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactions).executeWithoutResult(status -> {
            var issue = mes("rollback-position", "-3", 11L); service.prepare(issue); service.commit(issue);
            throw new IllegalStateException("downstream failure");
        }));
        assertBalance("10", "0", "10"); assertPosition(11L, "8", "0");
    }

    @Test void positionDimensionsAndTenantCannotBeReboundOrForged() {
        putaway(11L, 1L, "6");
        assertThrows(IllegalArgumentException.class, () -> ensure(11L, 9L));
        var forged = movement("forged", "-1"); forged.setPositionId(11L);
        assertThrows(IllegalArgumentException.class, () -> service.prepare(forged));
        TenantContextHolder.setTenantId(2L);
        assertTrue(service.positions(List.of(11L)).isEmpty());
        TenantContextHolder.setTenantId(1L); assertPosition(11L, "6", "0");
    }

    @Test void concurrentIssuesCannotOversellOneBatchEvenWhenWarehouseHasOtherStock() throws Exception {
        putaway(11L, 1L, "4"); putaway(12L, 2L, "6");
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                String key = "batch-parallel-" + i;
                results.add(executor.submit(() -> {
                    TenantContextHolder.setTenantId(1L);
                    try { start.await(); service.prepare(mes(key, "-3", 11L)); return true; }
                    catch (IllegalArgumentException insufficient) { return false; }
                    finally { TenantContextHolder.clear(); }
                }));
            }
            start.countDown(); int successes = 0;
            for (Future<Boolean> result : results) if (result.get(20, TimeUnit.SECONDS)) successes++;
            assertEquals(1, successes); assertBalance("10", "3", "7"); assertPosition(11L, "4", "3");
        }
    }
    private void ensure(Long stock, Long batch) {
        service.ensurePosition(new WmsStockApi.Position().setStockId(stock).setProductId(400L).setWarehouseId(500L)
                .setLocationId(1L).setAreaId(1L).setBatchId(batch).setBatchCode("B" + batch).setUnit("件"));
    }
    private void putaway(Long stock, Long batch, String quantity) {
        ensure(stock, batch); var request = mes("putaway-" + stock, quantity, stock); request.setAllocationOnly(true);
        service.prepare(request); service.commit(request);
    }
    private WmsStockApi.Movement mes(String key, String quantity, Long position) {
        var request = movement(key, quantity); request.setSource("MES"); request.setProductId(400L); request.setWarehouseId(500L); request.setPositionId(position); return request;
    }
    private void assertPosition(Long stock, String quantity, String reserved) {
        var position = service.positions(List.of(stock)).getFirst();
        assertEquals(0, new BigDecimal(quantity).compareTo(position.getQuantity()));
        assertEquals(0, new BigDecimal(reserved).compareTo(position.getReservedQuantity()));
        assertEquals(0, new BigDecimal(quantity).subtract(new BigDecimal(reserved)).compareTo(position.getAvailableQuantity()));
    }
    private void assertBalance(String quantity, String reserved, String available) {
        var balance = service.balance("MALL", 300L, 1L);
        assertEquals(0, new BigDecimal(quantity).compareTo(balance.getQuantity()));
        assertEquals(0, new BigDecimal(reserved).compareTo(balance.getReservedQuantity()));
        assertEquals(0, new BigDecimal(available).compareTo(balance.getAvailableQuantity()));
    }
}
