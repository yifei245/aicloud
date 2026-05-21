package com.aicloud.module.bpm.biz.controller;

import com.aicloud.module.bpm.biz.entity.AiBpmProcessDefinition;
import com.aicloud.module.bpm.biz.entity.AiBpmProcessInstance;
import com.aicloud.module.bpm.biz.entity.AiBpmTask;
import com.aicloud.module.bpm.biz.model.ApiResponse;
import com.aicloud.module.bpm.biz.model.BpmDefinitionSaveRequest;
import com.aicloud.module.bpm.biz.model.BpmInstanceStartRequest;
import com.aicloud.module.bpm.biz.model.BpmTaskCompleteRequest;
import com.aicloud.module.bpm.biz.service.BpmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "工作流")
@RestController
@RequestMapping("/bpm")
public class BpmController {

    private final BpmService bpmService;

    public BpmController(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Operation(summary = "流程定义列表")
    @GetMapping("/process-definition/list")
    public ApiResponse<List<AiBpmProcessDefinition>> definitions(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId) {
        return ApiResponse.ok(bpmService.definitionList(tenantId));
    }

    @Operation(summary = "创建或更新流程定义")
    @PostMapping({"/process-definition/create", "/process-definition/update"})
    public ApiResponse<AiBpmProcessDefinition> saveDefinition(@Valid @RequestBody BpmDefinitionSaveRequest body) {
        return ApiResponse.ok(bpmService.saveDefinition(body));
    }

    @Operation(summary = "发起流程")
    @PostMapping("/process/start")
    public ApiResponse<AiBpmProcessInstance> start(@Valid @RequestBody BpmInstanceStartRequest body) {
        return ApiResponse.ok(bpmService.start(body));
    }

    @Operation(summary = "流程实例列表")
    @GetMapping("/process-instance/list")
    public ApiResponse<List<AiBpmProcessInstance>> instances(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(bpmService.instanceList(tenantId, status));
    }

    @Operation(summary = "我的待办")
    @GetMapping("/task/todo")
    public ApiResponse<List<AiBpmTask>> todo(
            @RequestParam(name = "tenantId", defaultValue = "1") Long tenantId,
            @RequestParam(name = "assignee", required = false) String assignee) {
        return ApiResponse.ok(bpmService.todoList(tenantId, assignee));
    }

    @Operation(summary = "完成任务")
    @PostMapping("/task/complete")
    public ApiResponse<AiBpmTask> complete(@Valid @RequestBody BpmTaskCompleteRequest body) {
        return ApiResponse.ok(bpmService.complete(body.getTaskId()));
    }
}
