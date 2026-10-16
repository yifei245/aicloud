package com.aicloud.framework.mybatis.core.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationDeliveryTest {
    private AnnotationConfigApplicationContext context;
    private JdbcTemplate jdbc;
    private IntegrationInboxService inbox;
    private TransactionTemplate transaction;

    @BeforeEach void setup() throws Exception {
        context = new AnnotationConfigApplicationContext(Config.class);
        jdbc = context.getBean(JdbcTemplate.class);
        inbox = context.getBean(IntegrationInboxService.class);
        transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        Path root = Path.of("").toAbsolutePath();
        while (!Files.exists(root.resolve("sql/mysql/10-integration.sql"))) root = root.getParent();
        String sql = Files.readString(root.resolve("sql/mysql/10-integration.sql")).replace("USE aicloud;", "");
        try (var connection = context.getBean(DataSource.class).getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8)));
        }
        jdbc.execute("CREATE TABLE business_result(id BIGINT AUTO_INCREMENT PRIMARY KEY,tenant_id BIGINT,amount INT)");
    }
    @AfterEach void close() { context.close(); }

    @Test void sourceRollbackDoesNotLeaveAnEventOrBusinessWrite() {
        IntegrationOutboxService outbox = outbox(e -> {});
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            jdbc.update("INSERT INTO business_result(tenant_id,amount) VALUES(1,10)");
            outbox.enqueue(1L, "TEST", "source-1", Map.of("amount", 10));
            throw new IllegalStateException("Source approval rolls back");
        }));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM integration_outbox", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM business_result", Integer.class));
    }

    @Test void targetRollbackAllowsRetryAndSuccessfulDeliveryIsNotDuplicated() {
        AtomicInteger calls = new AtomicInteger();
        IntegrationOutboxService outbox = outbox(e -> inbox.execute(e.getTenantId(), "TEST", e.getBusinessKey(), e.getPayload(), Long.class, () -> {
            jdbc.update("INSERT INTO business_result(tenant_id,amount) VALUES(?,10)", e.getTenantId());
            if (calls.incrementAndGet() == 1) throw new IllegalStateException("Target fails before commit");
            return 42L;
        }));
        outbox.enqueue(1L, "TEST", "source-1", Map.of("amount", 10));
        outbox.dispatch();
        assertEquals("RETRY", jdbc.queryForObject("SELECT status FROM integration_outbox", String.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM business_result", Integer.class));
        jdbc.update("UPDATE integration_outbox SET next_attempt_time=TIMESTAMP '2000-01-01 00:00:00'");
        outbox.dispatch();
        outbox.dispatch();
        assertEquals("DONE", jdbc.queryForObject("SELECT status FROM integration_outbox", String.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM business_result", Integer.class));
        Long replay = inbox.execute(1L, "TEST", "source-1", "{\"amount\":10}", Long.class, () -> { fail("Duplicate must not run target action"); return 0L; });
        assertEquals(42L, replay);
    }

    @Test void tenantAndPayloadChecksPreventCrossTenantDeduplicationOrChangedReplay() {
        for (long tenant : new long[]{1, 2}) {
            inbox.execute(tenant, "TEST", "same-key", Map.of("amount", 10), Long.class, () -> {
                jdbc.update("INSERT INTO business_result(tenant_id,amount) VALUES(?,10)", tenant);
                return tenant;
            });
        }
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM business_result", Integer.class));
        assertThrows(IllegalArgumentException.class, () -> inbox.execute(1L, "TEST", "same-key", Map.of("amount", 20), Long.class, () -> 0L));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM business_result", Integer.class));
    }

    private IntegrationOutboxService outbox(java.util.function.Consumer<IntegrationEvent> delivery) {
        return new IntegrationOutboxService(jdbc, "producer-test", () -> List.of(new IntegrationEventHandler() {
            @Override public String eventType() { return "TEST"; }
            @Override public void handle(IntegrationEvent event) { delivery.accept(event); }
        }));
    }
    @Configuration @EnableTransactionManagement
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean JdbcTemplate jdbc(DataSource dataSource) { return new JdbcTemplate(dataSource); }
        @Bean PlatformTransactionManager transactionManager(DataSource dataSource) { return new DataSourceTransactionManager(dataSource); }
        @Bean IntegrationInboxService inbox(JdbcTemplate jdbc) { return new IntegrationInboxService(jdbc); }
    }
}
