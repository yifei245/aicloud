package com.aicloud.module.system.controller.app.oauth2;

import com.aicloud.module.system.controller.admin.oauth2.OAuth2OpenController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 会员 SSO 复用同一授权服务，入口由框架添加 /app-api 前缀。 */
@RestController
@RequestMapping("/system/oauth2")
@Tag(name = "用户端 - OAuth2/SSO")
public class AppOAuth2OpenController extends OAuth2OpenController {
}
