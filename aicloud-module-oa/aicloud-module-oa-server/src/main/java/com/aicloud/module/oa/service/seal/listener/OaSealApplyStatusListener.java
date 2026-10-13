package com.aicloud.module.oa.service.seal.listener;

import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.aicloud.module.oa.enums.BpmModelConstants;
import com.aicloud.module.oa.service.seal.OaSealApplyService;
import jakarta.annotation.Resource;
import com.aicloud.module.oa.enums.ApiConstants;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用印申请审批结果监听器
 *
 * @author 芋道源码
 */
@RestController
@Validated
public class OaSealApplyStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaSealApplyService sealApplyService;

    @Override
    public String getProcessDefinitionKey() {
        return BpmModelConstants.SEAL_APPLY;
    }

    @Override
    @PostMapping(ApiConstants.PREFIX + "/seal-apply/update-audit-status") // 提供给 bpm-server RPC 调用
    protected void onEvent(@RequestBody BpmProcessInstanceStatusEvent event) {
        sealApplyService.updateSealApplyStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
