package com.aicloud.module.product.framework.rpc.config;

import com.aicloud.module.member.api.level.MemberLevelApi;
import com.aicloud.module.member.api.user.MemberUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "productRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {MemberUserApi.class, MemberLevelApi.class, com.aicloud.module.wms.api.integration.WmsStockApi.class})
public class RpcConfiguration {
}
