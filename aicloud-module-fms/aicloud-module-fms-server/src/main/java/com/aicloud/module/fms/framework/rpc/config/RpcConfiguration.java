package com.aicloud.module.fms.framework.rpc.config;

import com.aicloud.module.infra.api.file.FileApi;
import com.aicloud.module.system.api.dept.DeptApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "fmsRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, DeptApi.class, FileApi.class})
public class RpcConfiguration {
}
