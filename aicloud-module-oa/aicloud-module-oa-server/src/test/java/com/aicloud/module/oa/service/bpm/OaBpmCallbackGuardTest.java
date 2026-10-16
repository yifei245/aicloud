package com.aicloud.module.oa.service.bpm;

import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class OaBpmCallbackGuardTest {
    AnnotationConfigApplicationContext context;
    JdbcTemplate jdbc;
    OaBpmCallbackGuard guard;
    @BeforeEach void setup() {
        context = new AnnotationConfigApplicationContext(Config.class);
        jdbc = context.getBean(JdbcTemplate.class); guard = context.getBean(OaBpmCallbackGuard.class);
        jdbc.execute("CREATE TABLE oa_leave_apply(id BIGINT PRIMARY KEY,tenant_id BIGINT,deleted INT,status INT,process_instance_id VARCHAR(100))");
        jdbc.update("INSERT INTO oa_leave_apply VALUES(1,1,0,1,'current-process')");
        TenantContextHolder.setTenantId(1L);
    }
    @AfterEach void close() { TenantContextHolder.clear(); context.close(); }
    @Test void duplicateAndStaleCallbacksCannotChangeTheCommittedOutcome() {
        AtomicInteger calls = new AtomicInteger();
        guard.execute("oa_leave_apply", event("old-process"), calls::incrementAndGet);
        assertEquals(0, calls.get());
        Runnable action = () -> { calls.incrementAndGet(); jdbc.update("UPDATE oa_leave_apply SET status=2 WHERE id=1"); };
        guard.execute("oa_leave_apply", event("current-process"), action);
        guard.execute("oa_leave_apply", event("current-process"), action);
        assertEquals(1, calls.get());
    }
    @Test void businessFailureRollsBackSoTheSameCallbackCanRetry() {
        assertThrows(IllegalStateException.class, () -> guard.execute("oa_leave_apply", event("current-process"), () -> {
            jdbc.update("UPDATE oa_leave_apply SET status=2 WHERE id=1"); throw new IllegalStateException("business effect failed");
        }));
        assertEquals(1, jdbc.queryForObject("SELECT status FROM oa_leave_apply WHERE id=1", Integer.class));
        guard.execute("oa_leave_apply", event("current-process"), () -> jdbc.update("UPDATE oa_leave_apply SET status=2 WHERE id=1"));
        assertEquals(2, jdbc.queryForObject("SELECT status FROM oa_leave_apply WHERE id=1", Integer.class));
    }
    @Test void wrongTenantAndUnexpectedDefinitionsCannotExecuteBusinessEffects() {
        TenantContextHolder.setTenantId(2L);
        guard.execute("oa_leave_apply", event("current-process"), () -> fail("Cross-tenant callback"));
        assertThrows(IllegalArgumentException.class, () -> guard.execute("oa_leave_apply;DELETE FROM oa_leave_apply", event("current-process"), () -> fail("Invalid SQL target")));
        var wrong = event("current-process"); wrong.setProcessDefinitionKey("oa_resign_apply");
        assertThrows(IllegalArgumentException.class, () -> guard.execute("oa_leave_apply", wrong, () -> fail("Wrong process")));
    }
    @Test void callbackWaitsForTheSourceSubmissionToCommitItsProcessBinding() throws Exception {
        var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        var invoked = new AtomicInteger();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var result = new java.util.concurrent.atomic.AtomicReference<Future<?>>();
            tx.executeWithoutResult(status -> {
                jdbc.update("UPDATE oa_leave_apply SET process_instance_id='new-process' WHERE id=1");
                result.set(executor.submit(() -> {
                    TenantContextHolder.setTenantId(1L);
                    try { guard.execute("oa_leave_apply", event("new-process"), invoked::incrementAndGet); }
                    finally { TenantContextHolder.clear(); }
                }));
                assertThrows(TimeoutException.class, () -> result.get().get(150,TimeUnit.MILLISECONDS));
                assertEquals(0, invoked.get());
            });
            result.get().get(5,TimeUnit.SECONDS);
            assertEquals(1, invoked.get());
        }
    }
    private BpmProcessInstanceStatusEvent event(String process) {
        var event = new BpmProcessInstanceStatusEvent(); event.setId(process); event.setBusinessKey("1");
        event.setProcessDefinitionKey("oa_leave_apply"); event.setStatus(2); return event;
    }
    @Configuration @EnableTransactionManagement static class Config {
        @Bean DataSource dataSource() { return new DriverManagerDataSource("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", ""); }
        @Bean JdbcTemplate jdbcTemplate(DataSource source) { return new JdbcTemplate(source); }
        @Bean PlatformTransactionManager transactionManager(DataSource source) { return new DataSourceTransactionManager(source); }
        @Bean OaBpmCallbackGuard guard() { return new OaBpmCallbackGuard(); }
    }
}
