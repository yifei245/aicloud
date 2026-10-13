package com.aicloud.module.pms.framework.rpc.config;

import com.aicloud.module.infra.api.file.FileApi;
import com.aicloud.module.system.api.dept.DeptApi;
import com.aicloud.module.system.api.notify.NotifyMessageSendApi;
import com.aicloud.module.system.api.permission.PermissionApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "pmsRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, DeptApi.class, PermissionApi.class,
        NotifyMessageSendApi.class, FileApi.class})
public class RpcConfiguration {
}
