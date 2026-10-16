package com.aicloud.module.wms.framework.rpc.config;

import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "wmsRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {AdminUserApi.class, com.aicloud.module.wms.api.integration.ErpStockIntentApi.class,
        com.aicloud.module.wms.api.integration.MesStockIntentApi.class, com.aicloud.module.wms.api.integration.MallStockIntentApi.class})
public class RpcConfiguration {
}
