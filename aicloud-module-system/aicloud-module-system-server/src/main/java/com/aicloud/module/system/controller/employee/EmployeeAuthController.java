package com.aicloud.module.system.controller.employee;
import com.aicloud.module.system.controller.admin.auth.AuthController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/system/auth")
@Tag(name="员工端 - 认证与单点登录")
public class EmployeeAuthController extends AuthController {}
