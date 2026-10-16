package com.aicloud.module.mes.framework.rpc.config;

import com.aicloud.module.system.api.dept.PostApi;
import com.aicloud.module.system.api.dict.DictDataApi;
import com.aicloud.module.system.api.permission.RoleApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "mesRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, PostApi.class, RoleApi.class, DictDataApi.class,
        com.aicloud.module.wms.api.integration.WmsStockApi.class})
@org.springframework.context.annotation.Import(com.aicloud.module.wms.api.integration.WmsStockSourceConfiguration.class)
public class RpcConfiguration {
}
