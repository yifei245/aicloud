package com.aicloud.module.hrm.controller.admin.attendance;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.hrm.service.attendance.record.HrmLeaveBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
@RestController
@RequestMapping("/hrm/attendance/leave-balance")
@Tag(name="管理后台 - HRM 假期额度")
public class HrmLeaveBalanceController {
    @Resource private HrmLeaveBalanceService service;
    @GetMapping("/list") @Operation(summary="查询员工假期余额、预占与使用量")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:leave:query')")
    public CommonResult<List<Map<String,Object>>> list(@RequestParam Long employeeId,@RequestParam int year) { return CommonResult.success(service.balances(employeeId,year)); }
    @GetMapping("/policies") @Operation(summary="查询 OA 与 HRM 统一假别")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:leave:query')")
    public CommonResult<List<Map<String,Object>>> policies() { return CommonResult.success(service.policies()); }
    @PostMapping("/grant") @Operation(summary="设置年度假期额度，不能低于使用与预占之和")
    @PreAuthorize("@ss.hasPermission('hrm:attendance:leave:update')")
    public CommonResult<Boolean> grant(@RequestBody @Valid Grant request) { service.grant(request.employeeId,request.code,request.year,request.entitlement); return CommonResult.success(true); }
    @Data public static class Grant {
        @NotNull @Positive private Long employeeId;
        @NotBlank private String code;
        @Min(2000) @Max(2100) private int year;
        @NotNull @DecimalMin("0") @Digits(integer=4,fraction=2) private BigDecimal entitlement;
    }
}
