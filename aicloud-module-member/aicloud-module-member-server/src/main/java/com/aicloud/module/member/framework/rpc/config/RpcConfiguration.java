package com.aicloud.module.member.framework.rpc.config;

import com.aicloud.module.system.api.logger.LoginLogApi;
import com.aicloud.module.system.api.sms.SmsCodeApi;
import com.aicloud.module.system.api.social.SocialClientApi;
import com.aicloud.module.system.api.social.SocialUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "memberRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {SmsCodeApi.class, LoginLogApi.class, SocialUserApi.class, SocialClientApi.class})
public class RpcConfiguration {
}
