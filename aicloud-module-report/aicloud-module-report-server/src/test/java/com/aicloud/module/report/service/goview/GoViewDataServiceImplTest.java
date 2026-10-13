package com.aicloud.module.report.service.goview;

import com.aicloud.framework.test.core.ut.BaseDbUnitTest;
import com.aicloud.module.report.controller.admin.goview.vo.data.GoViewDataRespVO;
import com.aicloud.framework.common.exception.ServiceException;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Import(GoViewDataServiceImpl.class)
public class GoViewDataServiceImplTest extends BaseDbUnitTest {

    @Resource
    private GoViewDataServiceImpl goViewDataService;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testGetDataBySQL() {
        // 准备参数
        String sql = "SELECT id, name FROM system_users";
        TenantContextHolder.setTenantId(1L);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("system_users"))).thenReturn(1);
        // mock 方法
        SqlRowSet sqlRowSet = mock(SqlRowSet.class);
        when(jdbcTemplate.queryForRowSet(contains("tenant_id = 1"))).thenReturn(sqlRowSet);
        // mock 元数据
        SqlRowSetMetaData metaData = mock(SqlRowSetMetaData.class);
        when(sqlRowSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnNames()).thenReturn(new String[]{"id", "name"});
        // mock 数据明细
        when(sqlRowSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(sqlRowSet.getObject("id")).thenReturn(1L).thenReturn(2L);
        when(sqlRowSet.getObject("name")).thenReturn("芋道源码").thenReturn("芋道");

        // 调用
        GoViewDataRespVO dataBySQL = goViewDataService.getDataBySQL(sql);
        // 断言
        assertEquals(Arrays.asList("id", "name"), dataBySQL.getDimensions());
        assertEquals(2, dataBySQL.getDimensions().size());
        assertEquals(2, dataBySQL.getSource().get(0).size());
        assertEquals(1L, dataBySQL.getSource().get(0).get("id"));
        assertEquals("芋道源码", dataBySQL.getSource().get(0).get("name"));
        assertEquals(2, dataBySQL.getSource().get(1).size());
        assertEquals(2L, dataBySQL.getSource().get(1).get("id"));
        assertEquals("芋道", dataBySQL.getSource().get(1).get("name"));
        TenantContextHolder.clear();
    }

    @Test
    public void rejectsWritesAndMultipleStatements() {
        assertThrows(ServiceException.class, () -> goViewDataService.getDataBySQL("DELETE FROM system_users"));
        assertThrows(ServiceException.class, () -> goViewDataService.getDataBySQL("SELECT 1; DELETE FROM system_users"));
        assertThrows(ServiceException.class, () -> goViewDataService.getDataBySQL("SELECT * FROM mysql.user"));
    }

}
