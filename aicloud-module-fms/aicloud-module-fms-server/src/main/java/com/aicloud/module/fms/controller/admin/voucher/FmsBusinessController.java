package com.aicloud.module.fms.controller.admin.voucher;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.security.core.util.SecurityFrameworkUtils;
import com.aicloud.module.fms.service.voucher.FmsBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/fms/business")
@Tag(name = "管理后台 - 财务业务来源")
public class FmsBusinessController {
    @Resource FmsBusinessService service;
    @PostMapping("/rule") @Operation(summary = "配置业务来源的账套与借贷科目")
    @PreAuthorize("@ss.hasPermission('fms:voucher:create')")
    public CommonResult<Boolean> rule(@RequestBody @Valid FmsBusinessService.Rule rule) { service.configure(rule, SecurityFrameworkUtils.getLoginUserId()); return CommonResult.success(true); }
    @GetMapping("/rules") @Operation(summary = "查询业务记账规则")
    @PreAuthorize("@ss.hasPermission('fms:voucher:query')")
    public CommonResult<List<Map<String, Object>>> rules(@RequestParam Long accountSetId) { return CommonResult.success(service.rules(accountSetId, SecurityFrameworkUtils.getLoginUserId())); }
    @GetMapping("/sources") @Operation(summary = "查询业务凭证与冲回记录")
    @PreAuthorize("@ss.hasPermission('fms:voucher:query')")
    public CommonResult<List<Map<String, Object>>> sources(@RequestParam Long accountSetId) { return CommonResult.success(service.sources(accountSetId, SecurityFrameworkUtils.getLoginUserId())); }
}
