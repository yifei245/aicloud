package com.aicloud.module.wms.api.integration;

import com.aicloud.framework.common.pojo.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface StockIntentApi {
    @GetMapping("/rpc-api/integration/stock-decision")
    CommonResult<String> decision(@RequestParam("businessKey") String businessKey);
}
