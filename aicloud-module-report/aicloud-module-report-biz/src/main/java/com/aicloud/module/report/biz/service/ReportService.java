package com.aicloud.module.report.biz.service;

import com.aicloud.module.report.biz.mapper.ReportDashboardMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class ReportService {

    private final ReportDashboardMapper mapper;

    public ReportService(ReportDashboardMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> operationDashboard(Long tenantId) {
        Map<String, Object> data = new HashMap<>(8);
        data.putAll(mapper.orderStats(tenantId));
        data.putAll(mapper.payStats(tenantId));
        data.putAll(mapper.memberStats(tenantId));
        data.putAll(mapper.customerStats(tenantId));
        data.putAll(mapper.merchantStats(tenantId));
        data.putAll(mapper.inventoryStats(tenantId));
        data.putAll(mapper.productStats(tenantId));
        return data;
    }

    public Map<String, Object> salesOverview(Long tenantId, String date) {
        Map<String, Object> data = new HashMap<>(8);
        data.put("date", date == null ? "today" : date);
        data.putAll(mapper.orderStats(tenantId));
        data.putAll(mapper.payStats(tenantId));
        data.put("orderStatusDistribution", mapper.orderStatusStats(tenantId));
        return data;
    }

    public List<Map<String, Object>> salesStatusDistribution(Long tenantId) {
        return mapper.orderStatusStats(tenantId);
    }

    public Map<String, Object> payOverview(Long tenantId) {
        Map<String, Object> data = new HashMap<>(4);
        data.putAll(mapper.payStats(tenantId));
        data.put("payStatusDistribution", mapper.payStatusStats(tenantId));
        return data;
    }

    public Map<String, Object> memberOverview(Long tenantId) {
        Map<String, Object> data = new HashMap<>(4);
        data.putAll(mapper.memberStats(tenantId));
        data.put("levelDistribution", mapper.memberLevelStats(tenantId));
        return data;
    }

    public Map<String, Object> crmOverview(Long tenantId) {
        Map<String, Object> data = new HashMap<>(4);
        data.putAll(mapper.customerStats(tenantId));
        data.put("opportunityStageDistribution", mapper.opportunityStageStats(tenantId));
        return data;
    }

    public Map<String, Object> merchantOverview(Long tenantId) {
        return mapper.merchantStats(tenantId);
    }

    public Map<String, Object> inventoryOverview(Long tenantId) {
        return mapper.inventoryStats(tenantId);
    }

    public Map<String, Object> businessFunnel(Long tenantId) {
        Map<String, Object> data = new HashMap<>(8);
        data.put("member", mapper.memberStats(tenantId));
        data.put("product", mapper.productStats(tenantId));
        data.put("order", mapper.orderStats(tenantId));
        data.put("pay", mapper.payStats(tenantId));
        data.put("crm", mapper.customerStats(tenantId));
        return data;
    }
}
