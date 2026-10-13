package com.aicloud.module.oa.service.regular.listener;

import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.aicloud.module.oa.enums.BpmModelConstants;
import com.aicloud.module.oa.service.regular.OaRegularApplyService;
import jakarta.annotation.Resource;
import com.aicloud.module.oa.enums.ApiConstants;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 转正申请审批结果监听器
 *
 * @author 芋道源码
 */
@RestController
@Validated
public class OaRegularApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaRegularApplyService regularApplyService;

    @Override
    public String getProcessDefinitionKey() {
        return BpmModelConstants.REGULAR_APPLY;
    }

    @Override
    @PostMapping(ApiConstants.PREFIX + "/regular-apply/update-audit-status") // 提供给 bpm-server RPC 调用
    protected void onEvent(@RequestBody BpmProcessInstanceStatusEvent event) {
        regularApplyService.updateRegularApplyStatus(Long.valueOf(event.getBusinessKey()), event.getStatus());
    }

}
