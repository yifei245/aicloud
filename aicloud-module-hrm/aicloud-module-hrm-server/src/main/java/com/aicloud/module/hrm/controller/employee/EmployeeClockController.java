package com.aicloud.module.hrm.controller.employee;

import com.aicloud.module.hrm.controller.admin.portal.attendance.HrmPortalAttendanceClockController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/** Reuses the existing self-service rules, permissions and ownership checks. */
@RestController
@RequestMapping("/hrm/attendance/clock")
@Tag(name="员工端 - attendance/clock")
public class EmployeeClockController extends HrmPortalAttendanceClockController {}
