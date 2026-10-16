package com.aicloud.module.oa.service.meetingroom.listener;

import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.aicloud.module.oa.enums.BpmModelConstants;
import com.aicloud.module.oa.service.meetingroom.OaMeetingRoomBookingService;
import jakarta.annotation.Resource;
import com.aicloud.module.oa.enums.ApiConstants;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 会议室预定审批结果监听器
 *
 * @author 芋道源码
 */
@RestController
@Validated
public class OaMeetingRoomBookingStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private com.aicloud.module.oa.service.bpm.OaBpmCallbackGuard callbacks;

    @Resource
    private OaMeetingRoomBookingService meetingRoomBookingService;

    @Override
    public String getProcessDefinitionKey() {
        return BpmModelConstants.MEETING_ROOM_BOOKING;
    }

    @Override
    @PostMapping(ApiConstants.PREFIX + "/meeting-room-booking/update-audit-status") // 提供给 bpm-server RPC 调用
    protected void onEvent(@RequestBody BpmProcessInstanceStatusEvent event) {
        callbacks.execute(getProcessDefinitionKey(), event, () -> {
            meetingRoomBookingService.updateMeetingRoomBookingStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
        });
    }
}
