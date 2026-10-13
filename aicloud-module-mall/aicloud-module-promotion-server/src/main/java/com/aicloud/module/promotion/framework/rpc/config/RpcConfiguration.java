package com.aicloud.module.promotion.framework.rpc.config;

import com.aicloud.module.infra.api.websocket.WebSocketSenderApi;
import com.aicloud.module.member.api.user.MemberUserApi;
import com.aicloud.module.product.api.category.ProductCategoryApi;
import com.aicloud.module.product.api.sku.ProductSkuApi;
import com.aicloud.module.product.api.spu.ProductSpuApi;
import com.aicloud.module.system.api.social.SocialClientApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import com.aicloud.module.trade.api.order.TradeOrderApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "promotionRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {ProductSkuApi.class, ProductSpuApi.class, ProductCategoryApi.class,
        MemberUserApi.class, TradeOrderApi.class, AdminUserApi.class, SocialClientApi.class,
        WebSocketSenderApi.class})
public class RpcConfiguration {
}
