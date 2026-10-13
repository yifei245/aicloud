package com.aicloud.module.hrm.framework.rpc.config;

import com.aicloud.module.bpm.api.task.BpmProcessInstanceApi;
import com.aicloud.module.system.api.dept.DeptApi;
import com.aicloud.module.system.api.logger.OperateLogApi;
import com.aicloud.module.system.api.notify.NotifyMessageSendApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "hrmRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, DeptApi.class, OperateLogApi.class,
        NotifyMessageSendApi.class, BpmProcessInstanceApi.class})
public class RpcConfiguration {
}
