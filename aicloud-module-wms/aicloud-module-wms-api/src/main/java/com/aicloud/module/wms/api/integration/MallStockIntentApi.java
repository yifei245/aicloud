package com.aicloud.module.wms.api.integration;
@org.springframework.cloud.openfeign.FeignClient(name = "trade-server", contextId = "wmsMallStockIntent")
public interface MallStockIntentApi extends StockIntentApi { }
