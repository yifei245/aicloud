package com.aicloud.module.bpm.controller.employee;

import com.aicloud.framework.common.pojo.*;
import com.aicloud.module.bpm.controller.admin.task.BpmTaskController;
import com.aicloud.module.bpm.controller.admin.task.vo.task.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bpm/tasks")
@Tag(name="员工端 - 本人审批待办")
public class EmployeeTaskController {
    @Resource private BpmTaskController tasks;
    @GetMapping("/todo-page") @Operation(summary="本人待办分页")
    public CommonResult<PageResult<BpmTaskRespVO>> todo(@Valid BpmTaskPageReqVO request) { return tasks.getTaskTodoPage(request); }
    @GetMapping("/done-page") @Operation(summary="本人已办分页")
    public CommonResult<PageResult<BpmTaskRespVO>> done(@Valid BpmTaskPageReqVO request) { return tasks.getTaskDonePage(request); }
    @PutMapping("/approve") @Operation(summary="通过分配给本人的审批任务")
    public CommonResult<Boolean> approve(@RequestBody @Valid BpmTaskApproveReqVO request) { return tasks.approveTask(request); }
    @PutMapping("/reject") @Operation(summary="驳回分配给本人的审批任务")
    public CommonResult<Boolean> reject(@RequestBody @Valid BpmTaskRejectReqVO request) { return tasks.rejectTask(request); }
}
