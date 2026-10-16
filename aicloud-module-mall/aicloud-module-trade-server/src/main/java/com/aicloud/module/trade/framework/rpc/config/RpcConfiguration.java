package com.aicloud.module.trade.framework.rpc.config;

import com.aicloud.module.member.api.address.MemberAddressApi;
import com.aicloud.module.member.api.config.MemberConfigApi;
import com.aicloud.module.member.api.level.MemberLevelApi;
import com.aicloud.module.member.api.point.MemberPointApi;
import com.aicloud.module.member.api.user.MemberUserApi;
import com.aicloud.module.pay.api.order.PayOrderApi;
import com.aicloud.module.pay.api.refund.PayRefundApi;
import com.aicloud.module.pay.api.transfer.PayTransferApi;
import com.aicloud.module.pay.api.wallet.PayWalletApi;
import com.aicloud.module.product.api.category.ProductCategoryApi;
import com.aicloud.module.product.api.comment.ProductCommentApi;
import com.aicloud.module.product.api.sku.ProductSkuApi;
import com.aicloud.module.product.api.spu.ProductSpuApi;
import com.aicloud.module.promotion.api.bargain.BargainActivityApi;
import com.aicloud.module.promotion.api.bargain.BargainRecordApi;
import com.aicloud.module.promotion.api.combination.CombinationRecordApi;
import com.aicloud.module.promotion.api.coupon.CouponApi;
import com.aicloud.module.promotion.api.discount.DiscountActivityApi;
import com.aicloud.module.promotion.api.point.PointActivityApi;
import com.aicloud.module.promotion.api.reward.RewardActivityApi;
import com.aicloud.module.promotion.api.seckill.SeckillActivityApi;
import com.aicloud.module.system.api.notify.NotifyMessageSendApi;
import com.aicloud.module.system.api.social.SocialClientApi;
import com.aicloud.module.system.api.social.SocialUserApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "tradeRpcConfiguration", proxyBeanMethods = false)
@org.springframework.context.annotation.Import({com.aicloud.module.wms.api.integration.WmsStockSourceConfiguration.class, com.aicloud.module.fms.api.business.FmsBusinessSourceConfiguration.class})
@EnableFeignClients(clients = {
        com.aicloud.module.fms.api.business.FmsBusinessApi.class,
        com.aicloud.module.wms.api.integration.WmsStockApi.class,
        BargainActivityApi.class, BargainRecordApi.class, CombinationRecordApi.class,
        CouponApi.class, DiscountActivityApi.class, RewardActivityApi.class, SeckillActivityApi.class, PointActivityApi.class,
        MemberUserApi.class, MemberPointApi.class, MemberLevelApi.class, MemberAddressApi.class, MemberConfigApi.class,
        ProductSpuApi.class, ProductSkuApi.class, ProductCommentApi.class, ProductCategoryApi.class,
        PayOrderApi.class, PayRefundApi.class, PayTransferApi.class, PayWalletApi.class,
        AdminUserApi.class, NotifyMessageSendApi.class, SocialClientApi.class, SocialUserApi.class
})
public class RpcConfiguration {
}
