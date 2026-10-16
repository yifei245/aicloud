package com.aicloud.module.wms.api.integration;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.mybatis.core.integration.IntegrationOutboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.RestController;

/** Imported only by ERP, MES and trade source services. */
@Configuration(proxyBeanMethods = false)
@org.springframework.boot.autoconfigure.condition.ConditionalOnExpression("'${spring.application.name}' == 'erp-server' || '${spring.application.name}' == 'mes-server' || '${spring.application.name}' == 'trade-server'")
public class WmsStockSourceConfiguration {
    @Bean
    WmsStockBridge wmsStockBridge(JdbcTemplate jdbc, WmsStockApi api, IntegrationOutboxService outbox,
                                  PlatformTransactionManager manager, @Value("${spring.application.name}") String producer) {
        String source = switch (producer) { case "erp-server" -> "ERP"; case "mes-server" -> "MES"; case "trade-server" -> "MALL";
            default -> throw new IllegalArgumentException("未知库存源服务：" + producer); };
        return new WmsStockBridge(jdbc, api, outbox, manager, source);
    }

    @RestController
    @org.springframework.boot.autoconfigure.condition.ConditionalOnExpression("'${spring.application.name}' == 'erp-server' || '${spring.application.name}' == 'mes-server' || '${spring.application.name}' == 'trade-server'")
    public static class StockIntentController implements StockIntentApi {
        private final WmsStockBridge bridge;
        public StockIntentController(WmsStockBridge bridge) { this.bridge = bridge; }
        @Override public CommonResult<String> decision(String businessKey) { return CommonResult.success(bridge.decision(businessKey)); }
    }
}
