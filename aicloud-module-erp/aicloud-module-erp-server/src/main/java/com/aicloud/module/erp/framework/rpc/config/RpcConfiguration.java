package com.aicloud.module.erp.framework.rpc.config;

import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "erpRpcConfiguration", proxyBeanMethods = false)
@org.springframework.context.annotation.Import({com.aicloud.module.fms.api.business.FmsBusinessSourceConfiguration.class, com.aicloud.module.wms.api.integration.WmsStockSourceConfiguration.class})
@EnableFeignClients(clients = {
        com.aicloud.module.fms.api.business.FmsBusinessApi.class,AdminUserApi.class, com.aicloud.module.wms.api.integration.WmsStockApi.class})
public class RpcConfiguration {
}
