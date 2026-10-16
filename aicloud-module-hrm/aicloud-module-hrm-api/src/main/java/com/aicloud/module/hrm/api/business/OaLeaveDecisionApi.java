package com.aicloud.module.hrm.api.business;
import com.aicloud.framework.common.pojo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name="oa-server",contextId="hrmOaLeaveDecision")
public interface OaLeaveDecisionApi {
    @GetMapping("/rpc-api/oa/leave/integration-decision") CommonResult<HrmOaApi.Action> decision(@RequestParam("key") String key);
}
