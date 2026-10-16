package com.aicloud.module.hrm.framework.rpc.config;

import com.aicloud.module.bpm.api.task.BpmProcessInstanceApi;
import com.aicloud.module.system.api.dept.DeptApi;
import com.aicloud.module.system.api.logger.OperateLogApi;
import com.aicloud.module.system.api.notify.NotifyMessageSendApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "hrmRpcConfiguration", proxyBeanMethods = false)
@org.springframework.context.annotation.Import(com.aicloud.module.fms.api.business.FmsBusinessSourceConfiguration.class)
@EnableFeignClients(clients = {
        com.aicloud.module.hrm.api.business.OaLeaveDecisionApi.class,
        com.aicloud.module.fms.api.business.FmsBusinessApi.class,AdminUserApi.class, DeptApi.class, OperateLogApi.class,
        NotifyMessageSendApi.class, BpmProcessInstanceApi.class})
public class RpcConfiguration {
}
