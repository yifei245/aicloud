package com.aicloud.module.oa.service.vehicle.listener;

import com.aicloud.module.oa.service.vehicle.OaVehicleReturnService;

import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.aicloud.module.oa.enums.BpmModelConstants;
import jakarta.annotation.Resource;
import com.aicloud.module.oa.enums.ApiConstants;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEventListener;

/**
 * 还车申请审批结果监听器
 *
 * @author 芋道源码
 */
@RestController
@Validated
public class OaVehicleReturnStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaVehicleReturnService vehicleReturnService;

    @Override
    public String getProcessDefinitionKey() {
        return BpmModelConstants.VEHICLE_RETURN;
    }

    @Override
    @PostMapping(ApiConstants.PREFIX + "/vehicle-return/update-audit-status") // 提供给 bpm-server RPC 调用
    protected void onEvent(@RequestBody BpmProcessInstanceStatusEvent event) {
        vehicleReturnService.updateVehicleReturnStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
