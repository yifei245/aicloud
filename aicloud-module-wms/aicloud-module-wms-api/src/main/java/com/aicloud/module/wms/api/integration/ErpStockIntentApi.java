package com.aicloud.module.wms.api.integration;
@org.springframework.cloud.openfeign.FeignClient(name = "erp-server", contextId = "wmsErpStockIntent")
public interface ErpStockIntentApi extends StockIntentApi { }
