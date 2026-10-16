package com.aicloud.module.hrm.controller.employee;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.security.core.util.SecurityFrameworkUtils;
import com.aicloud.module.hrm.service.employee.info.HrmEmployeeService;
import com.aicloud.module.hrm.service.attendance.record.HrmLeaveBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@Validated
@RequestMapping("/hrm/leave-balances")
@Tag(name="员工端 - 本人假期余额")
public class EmployeeLeaveBalanceController {
    @Resource private HrmEmployeeService employees;
    @Resource private HrmLeaveBalanceService balances;
    @GetMapping @Operation(summary="查询本人年度假期的余额、预占和使用量")
    @PreAuthorize("@ss.hasPermission('hrm:portal:query')")
    public CommonResult<List<Map<String,Object>>> list(@RequestParam @Min(2000) @Max(2100) int year) {
        var employee=employees.validateEmployeeBySelf(SecurityFrameworkUtils.getLoginUserId());
        return CommonResult.success(balances.balances(employee.getId(),year));
    }
}
