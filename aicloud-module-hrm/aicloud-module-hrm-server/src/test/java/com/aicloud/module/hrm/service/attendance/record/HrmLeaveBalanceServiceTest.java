package com.aicloud.module.hrm.service.attendance.record;

import com.aicloud.framework.test.core.ut.BaseDbUnitTest;
import com.aicloud.framework.mybatis.core.integration.IntegrationInboxService;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.hrm.api.business.HrmOaApi;
import com.aicloud.module.hrm.dal.dataobject.attendance.record.HrmAttendanceLeaveDO;
import com.aicloud.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import com.aicloud.module.hrm.dal.mysql.attendance.record.HrmAttendanceLeaveMapper;
import com.aicloud.module.hrm.service.employee.info.HrmEmployeeService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
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
import java.time.LocalDateTime;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Import({HrmLeaveBalanceService.class,IntegrationInboxService.class,HrmLeaveBalanceServiceTest.JdbcConfig.class})
class HrmLeaveBalanceServiceTest extends BaseDbUnitTest {
    @Resource HrmLeaveBalanceService service;
    @Resource HrmAttendanceLeaveMapper leaves;
    @Resource JdbcTemplate jdbc;
    @Resource DataSource source;
    @Resource PlatformTransactionManager transactions;
    @MockitoBean HrmEmployeeService employees;
    final LocalDateTime start=LocalDateTime.of(2026,10,20,9,0);
    @TestConfiguration static class JdbcConfig {
        @Bean JdbcTemplate jdbcTemplate(DataSource source) { return new JdbcTemplate(source); }
    }
    @BeforeEach void prepare() throws Exception {
        TenantContextHolder.setTenantId(1L);
        Path root=Path.of("").toAbsolutePath();
        while(!Files.exists(root.resolve("sql/mysql/10-integration.sql"))) root=root.getParent();
        String sql=Pattern.compile("CREATE TABLE IF NOT EXISTS `(?:hrm_leave_[^`]+|integration_inbox)`.*?;",Pattern.DOTALL)
                .matcher(Files.readString(root.resolve("sql/mysql/10-integration.sql"))).results().map(java.util.regex.MatchResult::group)
                .collect(java.util.stream.Collectors.joining("\n"));
        try(var connection=source.getConnection()) { ScriptUtils.executeSqlScript(connection,new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8))); }
        for(String table:new String[]{"hrm_leave_reservation","hrm_leave_balance","hrm_leave_policy","integration_inbox"}) jdbc.update("DELETE FROM "+table);
        var employee=new HrmEmployeeDO().setId(100L).setName("验收员工");
        when(employees.validateEmployeeBySelf(1L)).thenReturn(employee);
        when(employees.validateEmployeeExists(100L)).thenReturn(employee);
        service.grant(100L,"1",2026,new BigDecimal("3"));
    }
    @AfterEach void clear() { TenantContextHolder.clear(); }
    @Test void oaAndNativeHrmLeaveConsumeTheSameAnnualBalance() {
        var request=leave("oa-1"); Long oa=service.reserveOa(request); assertEquals(oa,service.reserveOa(request));
        balance("1","0","2");
        assertEquals("年假",leaves.selectById(oa).getType());
        service.finish("OA","oa-1",true,"process-1"); service.finish("OA","oa-1",true,"process-1");
        balance("0","1","2");
        var nativeLeave=nativeLeave(start.plusDays(1),"2");
        service.reserveNative(nativeLeave); balance("2","1","0");
        service.finish("HRM","leave:"+nativeLeave.getId(),true,"process-2"); balance("0","3","0");
        assertThrows(IllegalArgumentException.class,()->service.reserveOa(leave("insufficient").setStartTime(start.plusDays(3)).setEndTime(start.plusDays(3).plusHours(8))));
        balance("0","3","0");
    }
    @Test void rejectionReleasesQuotaAndBlocksLateReservation() {
        service.reserveOa(leave("cancel")); service.finish("OA","cancel",false,"rejected-process");
        service.finish("OA","cancel",false,"rejected-process"); balance("0","0","3");
        service.finish("OA","rollback-before-response",false,null);
        assertThrows(IllegalArgumentException.class,()->service.reserveOa(leave("rollback-before-response").setStartTime(start.plusDays(2)).setEndTime(start.plusDays(2).plusHours(8))));
        balance("0","0","3");
    }
    @Test void overlappingOaAndHrmRequestsCannotHoldQuotaTwice() {
        service.reserveOa(leave("oa-overlap"));
        var record=nativeLeave(start,"1");
        assertThrows(IllegalArgumentException.class,()->service.reserveNative(record));
        balance("1","0","2");
    }
    @Test void sourceFailureRollsBackNativeReservationAndFact() {
        var tx=new TransactionTemplate(transactions);
        assertThrows(IllegalStateException.class,()->tx.executeWithoutResult(status->{
            var record=nativeLeave(start,"1");service.reserveNative(record);throw new IllegalStateException("BPM rejected submission");
        }));
        balance("0","0","3"); assertEquals(0,leaves.selectCount());
    }
    @Test void tenantCannotConsumeAnotherTenantsBalanceAndUnknownPoliciesAreRejected() {
        TenantContextHolder.setTenantId(2L);
        assertThrows(IllegalArgumentException.class,()->service.reserveOa(leave("other-tenant")));
        TenantContextHolder.setTenantId(1L);balance("0","0","3");
        assertThrows(IllegalArgumentException.class,()->service.grant(100L,"unknown",2026,BigDecimal.ONE));
        assertEquals(1,service.balances(100L,2026).size());
    }
    private HrmOaApi.Leave leave(String key) { return new HrmOaApi.Leave().setKey(key).setSourceId(1L).setUserId(1L).setOaType(1).setDays(BigDecimal.ONE).setStartTime(start).setEndTime(start.plusHours(8)).setReason("验收"); }
    private HrmAttendanceLeaveDO nativeLeave(LocalDateTime time,String days) {
        var record=new HrmAttendanceLeaveDO().setEmployeeId(100L).setType("年假").setDay(new BigDecimal(days))
                .setStartTime(time).setEndTime(time.plusHours(8)).setReason("验收").setApprovalStatus(1);
        leaves.insert(record);return record;
    }
    private void balance(String reserved,String used,String available) {
        var row=service.balances(100L,2026).getFirst();
        for(var pair:new String[][]{{"reserved",reserved},{"used",used},{"available",available}}) assertEquals(0,new BigDecimal(pair[1]).compareTo((BigDecimal)row.get(pair[0])));
    }
}
