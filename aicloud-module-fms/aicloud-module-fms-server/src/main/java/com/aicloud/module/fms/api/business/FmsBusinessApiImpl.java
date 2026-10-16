package com.aicloud.module.fms.api.business;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.fms.service.voucher.FmsBusinessService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class FmsBusinessApiImpl implements FmsBusinessApi {
    @Resource FmsBusinessService service;
    @Override public CommonResult<Long> deliver(Event event) { return CommonResult.success(service.deliver(event)); }
}
