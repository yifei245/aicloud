package com.aicloud.module.crm.biz.controller;

import com.aicloud.module.crm.biz.entity.AiCrmCustomer;
import com.aicloud.module.crm.biz.entity.AiCrmFollowRecord;
import com.aicloud.module.crm.biz.entity.AiCrmOpportunity;
import com.aicloud.module.crm.biz.model.ApiResponse;
import com.aicloud.module.crm.biz.model.CrmCustomerSaveRequest;
import com.aicloud.module.crm.biz.model.CrmFollowSaveRequest;
import com.aicloud.module.crm.biz.model.CrmOpportunitySaveRequest;
import com.aicloud.module.crm.biz.model.PageResponse;
import com.aicloud.module.crm.biz.service.CrmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CRM 客户关系")
@RestController
@RequestMapping("/crm")
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class CrmController {

    private final CrmService crmService;

    public CrmController(CrmService crmService) {
        this.crmService = crmService;
    }

    @Operation(summary = "CRM 概览")
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview(@RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(crmService.overview(tenantId));
    }

    @Operation(summary = "客户分页")
    @GetMapping("/customer/list")
    public ApiResponse<PageResponse<AiCrmCustomer>> listCustomers(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "level", required = false) String level,
            @RequestParam(name = "source", required = false) String source,
            @RequestParam(name = "ownerUser", required = false) String ownerUser,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "20") long pageSize) {
        return ApiResponse.ok(crmService.listCustomers(tenantId, keyword, status, level, source, ownerUser, pageNo, pageSize));
    }

    @Operation(summary = "客户详情")
    @GetMapping("/customer/{id}")
    public ApiResponse<AiCrmCustomer> getCustomer(@PathVariable Long id) {
        return ApiResponse.ok(crmService.getCustomer(id));
    }

    @Operation(summary = "创建或更新客户")
    @PostMapping({"/customer/create", "/customer/update"})
    public ApiResponse<AiCrmCustomer> saveCustomer(@Valid @RequestBody CrmCustomerSaveRequest body) {
        return ApiResponse.ok(crmService.saveCustomer(body));
    }

    @Operation(summary = "客户状态变更")
    @PostMapping("/customer/status")
    public ApiResponse<AiCrmCustomer> updateCustomerStatus(@RequestParam Long id, @RequestParam String status) {
        return ApiResponse.ok(crmService.updateCustomerStatus(id, status));
    }

    @Operation(summary = "客户转移负责人")
    @PostMapping("/customer/transfer")
    public ApiResponse<AiCrmCustomer> transferCustomer(@RequestParam Long id, @RequestParam String ownerUser) {
        return ApiResponse.ok(crmService.transferCustomer(id, ownerUser));
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/customer/{id}")
    public ApiResponse<Boolean> deleteCustomer(@PathVariable Long id) {
        crmService.deleteCustomer(id);
        return ApiResponse.ok(Boolean.TRUE);
    }

    @Operation(summary = "跟进记录列表")
    @GetMapping("/follow/list")
    public ApiResponse<List<AiCrmFollowRecord>> listFollows(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "customerId") Long customerId) {
        return ApiResponse.ok(crmService.listFollows(tenantId, customerId));
    }

    @Operation(summary = "新增跟进记录")
    @PostMapping("/follow/create")
    public ApiResponse<AiCrmFollowRecord> saveFollow(@Valid @RequestBody CrmFollowSaveRequest body) {
        return ApiResponse.ok(crmService.saveFollow(body));
    }

    @Operation(summary = "商机列表")
    @GetMapping("/opportunity/list")
    public ApiResponse<List<AiCrmOpportunity>> listOpportunities(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "customerId", required = false) Long customerId,
            @RequestParam(name = "stage", required = false) String stage,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "ownerUser", required = false) String ownerUser) {
        return ApiResponse.ok(crmService.listOpportunities(tenantId, customerId, stage, status, ownerUser));
    }

    @Operation(summary = "商机详情")
    @GetMapping("/opportunity/{id}")
    public ApiResponse<AiCrmOpportunity> getOpportunity(@PathVariable Long id) {
        return ApiResponse.ok(crmService.getOpportunity(id));
    }

    @Operation(summary = "创建或更新商机")
    @PostMapping({"/opportunity/create", "/opportunity/update"})
    public ApiResponse<AiCrmOpportunity> saveOpportunity(@Valid @RequestBody CrmOpportunitySaveRequest body) {
        return ApiResponse.ok(crmService.saveOpportunity(body));
    }

    @Operation(summary = "商机阶段推进")
    @PostMapping("/opportunity/stage")
    public ApiResponse<AiCrmOpportunity> updateOpportunityStage(@RequestParam Long id, @RequestParam String stage) {
        return ApiResponse.ok(crmService.updateOpportunityStage(id, stage));
    }

    @Operation(summary = "商机状态变更")
    @PostMapping("/opportunity/status")
    public ApiResponse<AiCrmOpportunity> updateOpportunityStatus(@RequestParam Long id, @RequestParam String status) {
        return ApiResponse.ok(crmService.updateOpportunityStatus(id, status));
    }

    @Operation(summary = "删除商机")
    @DeleteMapping("/opportunity/{id}")
    public ApiResponse<Boolean> deleteOpportunity(@PathVariable Long id) {
        crmService.deleteOpportunity(id);
        return ApiResponse.ok(Boolean.TRUE);
    }
}
