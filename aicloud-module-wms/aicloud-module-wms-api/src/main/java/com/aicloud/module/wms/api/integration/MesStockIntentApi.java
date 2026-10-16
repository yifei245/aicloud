package com.aicloud.module.wms.api.integration;
@org.springframework.cloud.openfeign.FeignClient(name = "mes-server", contextId = "wmsMesStockIntent")
public interface MesStockIntentApi extends StockIntentApi { }
