package com.aicloud.module.report.service.goview;

import com.aicloud.module.report.controller.admin.goview.vo.data.GoViewDataRespVO;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import com.google.common.collect.Maps;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import static com.aicloud.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;

/**
 * GoView 数据 Service 实现类
 *
 * 补充说明：
 * 1. 目前默认使用 jdbcTemplate 查询项目配置的数据源。如果你想查询其它数据源，可以新建对应数据源的 jdbcTemplate 来实现。
 * 2. 默认数据源是 MySQL 关系数据源，可能数据量比较大的情况下，会比较慢，可以考虑后续使用 Click House 等等。
 *
 * @author 芋道源码
 */
@Service
@Validated
public class GoViewDataServiceImpl implements GoViewDataService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    void configureQueryLimits() {
        jdbcTemplate.setMaxRows(1000);
        jdbcTemplate.setQueryTimeout(10);
    }

    @Override
    public GoViewDataRespVO getDataBySQL(String sql) {
        sql = prepareQuery(sql);
        // 1. 执行查询
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);

        // 2. 构建返回结果
        GoViewDataRespVO respVO = new GoViewDataRespVO();
        // 2.1 解析元数据
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        String[] columnNames = metaData.getColumnNames();
        respVO.setDimensions(Arrays.asList(columnNames));
        // 2.2 解析数据明细
        respVO.setSource(new LinkedList<>()); // 由于数据量不确认，使用 LinkedList 虽然内存占用大一点，但是不存在扩容复制的问题
        while (sqlRowSet.next()) {
            Map<String, Object> data = Maps.newHashMapWithExpectedSize(columnNames.length);
            for (String columnName : columnNames) {
                data.put(columnName, sqlRowSet.getObject(columnName));
            }
            respVO.getSource().add(data);
        }
        return respVO;
    }

    private String prepareQuery(String sql) {
        Statement statement;
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sql);
            if (statements.getStatements().size() != 1) {
                throw invalidParamException("报表需要一条 SELECT 查询");
            }
            statement = statements.getStatements().get(0);
        } catch (Exception ex) {
            throw invalidParamException( "报表需要一条合法的 SELECT 查询");
        }
        if (!(statement instanceof Select)) {
            throw invalidParamException( "报表只允许 SELECT 查询");
        }
        Set<String> tenantTables = new HashSet<>();
        for (String table : new TablesNamesFinder<>().getTables(statement)) {
            String name = table.replace("`", "");
            if (name.contains(".")) {
                if (!name.startsWith("aicloud.")) {
                    throw invalidParamException( "报表只能查询 aicloud 数据库");
                }
                name = name.substring("aicloud.".length());
            }
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.columns "
                    + "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = 'tenant_id'",
                    Integer.class, name);
            if (count != null && count > 0) {
                tenantTables.add(name.toLowerCase());
            }
        }
        TenantLineInnerInterceptor tenant = new TenantLineInnerInterceptor(new TenantLineHandler() {
            public Expression getTenantId() {
                return new LongValue(TenantContextHolder.getRequiredTenantId());
            }
            public boolean ignoreTable(String tableName) {
                String name = tableName.replace("`", "");
                if (name.startsWith("aicloud.")) name = name.substring("aicloud.".length());
                return !tenantTables.contains(name.toLowerCase());
            }
        });
        return tenant.parserSingle(statement.toString(), null);
    }

}
