package com.aicloud.module.report.biz.controller;

import com.aicloud.common.pojo.ApiResponse;
import com.aicloud.module.report.biz.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "报表中心")
@RestController
@RequestMapping("/report")
/**
 * AICloud generated source.
 *
 * @author yifei
 */
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "经营看板")
    @GetMapping("/dashboard/operation")
    public ApiResponse<Map<String, Object>> operationDashboard(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(reportService.operationDashboard(tenantId));
    }

    @Operation(summary = "销售概览")
    @GetMapping("/sales/overview")
    public ApiResponse<Map<String, Object>> salesOverview(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "date", required = false) String date) {
        return ApiResponse.ok(reportService.salesOverview(tenantId, date));
    }

    @Operation(summary = "销售状态分布")
    @GetMapping("/sales/status-distribution")
    public ApiResponse<List<Map<String, Object>>> salesStatusDistribution(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(reportService.salesStatusDistribution(tenantId));
    }

    @Operation(summary = "支付概览")
    @GetMapping("/pay/overview")
    public ApiResponse<Map<String, Object>> payOverview(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(reportService.payOverview(tenantId));
    }

    @Operation(summary = "会员概览")
    @GetMapping("/member/overview")
    public ApiResponse<Map<String, Object>> memberOverview(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(reportService.memberOverview(tenantId));
    }

    @Operation(summary = "CRM 报表概览")
    @GetMapping("/crm/overview")
    public ApiResponse<Map<String, Object>> crmOverview(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(reportService.crmOverview(tenantId));
    }

    @Operation(summary = "商户概览")
    @GetMapping("/merchant/overview")
    public ApiResponse<Map<String, Object>> merchantOverview(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(reportService.merchantOverview(tenantId));
    }

    @Operation(summary = "库存概览")
    @GetMapping("/inventory/overview")
    public ApiResponse<Map<String, Object>> inventoryOverview(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(reportService.inventoryOverview(tenantId));
    }

    @Operation(summary = "业务漏斗")
    @GetMapping("/dashboard/funnel")
    public ApiResponse<Map<String, Object>> businessFunnel(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(reportService.businessFunnel(tenantId));
    }
}
