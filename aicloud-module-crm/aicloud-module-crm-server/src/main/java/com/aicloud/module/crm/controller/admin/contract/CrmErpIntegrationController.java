package com.aicloud.module.crm.controller.admin.contract;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.crm.service.contract.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/crm/integration")
@Tag(name="管理后台 - CRM 与 ERP 业务联动")
public class CrmErpIntegrationController {
    @Resource private CrmErpIntegration service;
    @Resource private CrmContractService contracts;
    @PostMapping("/bind") @Operation(summary="明确关联 CRM 客户、商品与 ERP 主档")
    @PreAuthorize("@ss.hasPermission('crm:contract:update') && @ss.hasPermission('erp:product:query') && @ss.hasPermission('erp:customer:query')")
    public CommonResult<Boolean> bind(@RequestBody @Valid Binding binding) {
        service.bind(binding.kind,binding.crmId,binding.erpId,binding.unit); return CommonResult.success(true);
    }
    @GetMapping("/contract-sale") @Operation(summary="查询合同审批后生成的 ERP 销售单编号")
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<Long> sale(@RequestParam Long contractId) {
        if(contracts.getContract(contractId)==null) throw new IllegalArgumentException("合同不存在");
        return CommonResult.success(service.saleOrder(contractId));
    }
    @Data public static class Binding {
        @NotBlank @Pattern(regexp="CUSTOMER|PRODUCT") private String kind;
        @NotNull @Positive private Long crmId;
        @NotNull @Positive private Long erpId;
        @Size(max=64) private String unit;
    }
}
