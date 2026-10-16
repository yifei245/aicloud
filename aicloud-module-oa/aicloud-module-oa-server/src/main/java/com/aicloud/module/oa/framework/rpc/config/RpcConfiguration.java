package com.aicloud.module.oa.framework.rpc.config;

import com.aicloud.module.bpm.api.task.BpmProcessInstanceApi;
import com.aicloud.module.infra.api.file.FileApi;
import com.aicloud.module.system.api.dept.DeptApi;
import com.aicloud.module.system.api.dict.DictDataApi;
import com.aicloud.module.system.api.notify.NotifyMessageSendApi;
import com.aicloud.module.system.api.permission.PermissionApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "oaRpcConfiguration", proxyBeanMethods = false)
@org.springframework.context.annotation.Import(com.aicloud.module.fms.api.business.FmsBusinessSourceConfiguration.class)
@EnableFeignClients(clients = {
        com.aicloud.module.hrm.api.business.HrmOaApi.class,
        com.aicloud.module.fms.api.business.FmsBusinessApi.class,BpmProcessInstanceApi.class, FileApi.class, DeptApi.class, DictDataApi.class,
        NotifyMessageSendApi.class, PermissionApi.class, AdminUserApi.class})
public class RpcConfiguration {
}
