package com.aicloud.module.oa.service.travel.listener;

import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.aicloud.module.oa.enums.BpmModelConstants;
import com.aicloud.module.oa.service.travel.OaTravelReimbursementService;
import jakarta.annotation.Resource;
import com.aicloud.module.oa.enums.ApiConstants;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 出差报销审批结果监听器
 *
 * @author 芋道源码
 */
@RestController
@Validated
public class OaTravelReimbursementStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private com.aicloud.module.oa.service.bpm.OaBpmCallbackGuard callbacks;

    @Resource
    private OaTravelReimbursementService travelReimbursementService;

    @Override
    public String getProcessDefinitionKey() {
        return BpmModelConstants.TRAVEL_REIMBURSEMENT;
    }

    @Override
    @PostMapping(ApiConstants.PREFIX + "/travel-reimbursement/update-audit-status") // 提供给 bpm-server RPC 调用
    protected void onEvent(@RequestBody BpmProcessInstanceStatusEvent event) {
        callbacks.execute(getProcessDefinitionKey(), event, () -> {
            travelReimbursementService.updateTravelReimbursementStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
        });
    }

}
