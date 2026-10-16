package com.aicloud.module.hrm.api.business;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.aicloud.module.hrm.service.attendance.record.HrmAttendanceLeaveService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
public class HrmLeaveApprovalController {
    @Resource HrmAttendanceLeaveService service;
    @PostMapping("/rpc-api/hrm/leave/process-status")
    public CommonResult<Boolean> status(@RequestBody @Valid BpmProcessInstanceStatusEvent event) {
        if(!"hrm_attendance_leave".equals(event.getProcessDefinitionKey())) throw new IllegalArgumentException("流程与请假申请不匹配");
        service.updateLeaveApprovalStatus(Long.valueOf(event.getBusinessKey()),event.getId(),event.getStatus(),event.getReason());
        return CommonResult.success(true);
    }
}
