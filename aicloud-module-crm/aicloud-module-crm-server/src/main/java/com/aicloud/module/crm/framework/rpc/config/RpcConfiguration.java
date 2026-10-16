package com.aicloud.module.crm.framework.rpc.config;

import com.aicloud.module.bpm.api.task.BpmProcessInstanceApi;
import com.aicloud.module.system.api.dept.DeptApi;
import com.aicloud.module.system.api.dept.PostApi;
import com.aicloud.module.system.api.logger.OperateLogApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "crmRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, DeptApi.class, PostApi.class,
        com.aicloud.module.erp.api.business.ErpBusinessApi.class,
        OperateLogApi.class,
        BpmProcessInstanceApi.class})
public class RpcConfiguration {
}
