package com.aicloud.module.hrm.controller.employee;

import com.aicloud.module.hrm.controller.admin.portal.employee.HrmPortalEmployeeController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/** Reuses the existing self-service rules, permissions and ownership checks. */
@RestController
@RequestMapping("/hrm/profile")
@Tag(name="员工端 - profile")
public class EmployeeProfileController extends HrmPortalEmployeeController {}
