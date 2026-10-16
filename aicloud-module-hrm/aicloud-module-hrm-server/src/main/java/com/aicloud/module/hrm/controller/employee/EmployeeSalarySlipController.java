package com.aicloud.module.hrm.controller.employee;

import com.aicloud.module.hrm.controller.admin.portal.salary.HrmPortalSalarySlipController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/** Reuses the existing self-service rules, permissions and ownership checks. */
@RestController
@RequestMapping("/hrm/salary/slip")
@Tag(name="员工端 - salary/slip")
public class EmployeeSalarySlipController extends HrmPortalSalarySlipController {}
