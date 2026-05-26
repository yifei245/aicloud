package com.aicloud.module.bpm.biz.service;

import com.aicloud.module.bpm.biz.entity.AiBpmProcessDefinition;
import com.aicloud.module.bpm.biz.entity.AiBpmProcessInstance;
import com.aicloud.module.bpm.biz.entity.AiBpmTask;
import com.aicloud.module.bpm.biz.mapper.BpmProcessDefinitionMapper;
import com.aicloud.module.bpm.biz.mapper.BpmProcessInstanceMapper;
import com.aicloud.module.bpm.biz.mapper.BpmTaskMapper;
import com.aicloud.module.bpm.biz.model.BpmDefinitionSaveRequest;
import com.aicloud.module.bpm.biz.model.BpmInstanceStartRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Flowable backed BPM service.
 *
 * @author yifei
 */
@Service
public class BpmService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_TODO = "TODO";
    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_FINISHED = "FINISHED";
    private static final String VARIABLE_ASSIGNEE = "assignee";

    private final BpmProcessDefinitionMapper definitionMapper;
    private final BpmProcessInstanceMapper instanceMapper;
    private final BpmTaskMapper taskMapper;
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final IdentityService identityService;

    public BpmService(BpmProcessDefinitionMapper definitionMapper,
                      BpmProcessInstanceMapper instanceMapper,
                      BpmTaskMapper taskMapper,
                      RepositoryService repositoryService,
                      RuntimeService runtimeService,
                      TaskService taskService,
                      IdentityService identityService) {
        this.definitionMapper = definitionMapper;
        this.instanceMapper = instanceMapper;
        this.taskMapper = taskMapper;
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.identityService = identityService;
    }

    public List<AiBpmProcessDefinition> definitionList(Long tenantId) {
        return definitionMapper.selectList(new LambdaQueryWrapper<AiBpmProcessDefinition>()
                .eq(AiBpmProcessDefinition::getTenantId, tenantId)
                .orderByDesc(AiBpmProcessDefinition::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiBpmProcessDefinition saveDefinition(BpmDefinitionSaveRequest request) {
        String tenantId = String.valueOf(request.getTenantId());
        String bpmnXml = StringUtils.hasText(request.getBpmnXml())
                ? request.getBpmnXml()
                : buildSingleApprovalBpmn(request);
        Deployment deployment = repositoryService.createDeployment()
                .tenantId(tenantId)
                .name(request.getProcessName())
                .category(request.getCategory())
                .addBytes(request.getProcessKey() + ".bpmn20.xml", bpmnXml.getBytes(StandardCharsets.UTF_8))
                .deploy();
        ProcessDefinition engineDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .processDefinitionKey(request.getProcessKey())
                .processDefinitionTenantId(tenantId)
                .singleResult();
        if (engineDefinition == null) {
            throw new IllegalArgumentException("Flowable 流程定义部署失败");
        }

        AiBpmProcessDefinition definition = request.getId() == null
                ? new AiBpmProcessDefinition()
                : definitionMapper.selectById(request.getId());
        if (definition == null) {
            throw new IllegalArgumentException("流程定义不存在");
        }
        definition.setTenantId(request.getTenantId());
        definition.setProcessKey(request.getProcessKey());
        definition.setProcessName(request.getProcessName());
        definition.setVersionNo(engineDefinition.getVersion());
        definition.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : STATUS_ACTIVE);
        definition.setCategory(request.getCategory());
        definition.setCreateBy(request.getCreateBy());
        definition.setDeploymentId(deployment.getId());
        definition.setEngineDefinitionId(engineDefinition.getId());
        definition.setUpdateTime(LocalDateTime.now());
        if (definition.getId() == null) {
            definition.setCreateTime(LocalDateTime.now());
            definitionMapper.insert(definition);
        } else {
            definitionMapper.updateById(definition);
        }
        return definition;
    }

    @Transactional(rollbackFor = Exception.class)
    public AiBpmProcessInstance start(BpmInstanceStartRequest request) {
        AiBpmProcessDefinition definition = latestActiveDefinition(request.getTenantId(), request.getProcessKey());
        if (definition == null || !StringUtils.hasText(definition.getEngineDefinitionId())) {
            throw new IllegalArgumentException("有效 Flowable 流程定义不存在");
        }

        Map<String, Object> variables = new HashMap<>();
        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }
        if (StringUtils.hasText(request.getCurrentAssignee())) {
            variables.put(VARIABLE_ASSIGNEE, request.getCurrentAssignee());
        }
        identityService.setAuthenticatedUserId(request.getStarter());
        try {
            ProcessInstance engineInstance = runtimeService.startProcessInstanceById(
                    definition.getEngineDefinitionId(), request.getBusinessId(), variables);
            AiBpmProcessInstance instance = createMirrorInstance(request, definition, engineInstance);
            syncActiveTasks(request.getTenantId(), instance, engineInstance.getId());
            return instance;
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    public List<AiBpmProcessInstance> instanceList(Long tenantId, String status) {
        return instanceMapper.selectList(new LambdaQueryWrapper<AiBpmProcessInstance>()
                .eq(AiBpmProcessInstance::getTenantId, tenantId)
                .eq(StringUtils.hasText(status), AiBpmProcessInstance::getStatus, status)
                .orderByDesc(AiBpmProcessInstance::getId));
    }

    public List<AiBpmTask> todoList(Long tenantId, String assignee) {
        return taskMapper.selectList(new LambdaQueryWrapper<AiBpmTask>()
                .eq(AiBpmTask::getTenantId, tenantId)
                .eq(StringUtils.hasText(assignee), AiBpmTask::getAssignee, assignee)
                .eq(AiBpmTask::getStatus, STATUS_TODO)
                .isNotNull(AiBpmTask::getEngineTaskId)
                .orderByDesc(AiBpmTask::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiBpmTask complete(Long taskId, Map<String, Object> variables) {
        AiBpmTask task = taskMapper.selectById(taskId);
        if (task == null || !StringUtils.hasText(task.getEngineTaskId())) {
            throw new IllegalArgumentException("Flowable 任务不存在");
        }
        Task engineTask = taskService.createTaskQuery().taskId(task.getEngineTaskId()).singleResult();
        if (engineTask == null) {
            throw new IllegalArgumentException("Flowable 任务已不存在或已完成");
        }
        taskService.complete(engineTask.getId(), variables == null ? Map.of() : variables);

        task.setStatus(STATUS_DONE);
        task.setCompleteTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        AiBpmProcessInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (instance != null) {
            if (runtimeService.createProcessInstanceQuery().processInstanceId(instance.getEngineInstanceId()).singleResult() == null) {
                instance.setStatus(STATUS_FINISHED);
                instance.setEndTime(LocalDateTime.now());
                instance.setCurrentAssignee(null);
                instance.setUpdateTime(LocalDateTime.now());
                instanceMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AiBpmProcessInstance>()
                        .eq(AiBpmProcessInstance::getId, instance.getId())
                        .set(AiBpmProcessInstance::getStatus, STATUS_FINISHED)
                        .set(AiBpmProcessInstance::getEndTime, instance.getEndTime())
                        .set(AiBpmProcessInstance::getCurrentAssignee, null)
                        .set(AiBpmProcessInstance::getUpdateTime, instance.getUpdateTime()));
            } else {
                syncActiveTasks(instance.getTenantId(), instance, instance.getEngineInstanceId());
            }
        }
        return task;
    }

    private AiBpmProcessDefinition latestActiveDefinition(Long tenantId, String processKey) {
        return definitionMapper.selectOne(new LambdaQueryWrapper<AiBpmProcessDefinition>()
                .eq(AiBpmProcessDefinition::getTenantId, tenantId)
                .eq(AiBpmProcessDefinition::getProcessKey, processKey)
                .eq(AiBpmProcessDefinition::getStatus, STATUS_ACTIVE)
                .isNotNull(AiBpmProcessDefinition::getEngineDefinitionId)
                .orderByDesc(AiBpmProcessDefinition::getVersionNo)
                .last("limit 1"));
    }

    private AiBpmProcessInstance createMirrorInstance(BpmInstanceStartRequest request,
                                                       AiBpmProcessDefinition definition,
                                                       ProcessInstance engineInstance) {
        AiBpmProcessInstance instance = new AiBpmProcessInstance();
        instance.setTenantId(request.getTenantId());
        instance.setInstanceNo("PI-" + shortUuid());
        instance.setProcessDefinitionId(definition.getId());
        instance.setProcessKey(request.getProcessKey());
        instance.setBusinessId(request.getBusinessId());
        instance.setStarter(request.getStarter());
        instance.setCurrentAssignee(request.getCurrentAssignee());
        instance.setStatus(STATUS_RUNNING);
        instance.setEngineInstanceId(engineInstance.getId());
        instance.setEngineDefinitionId(engineInstance.getProcessDefinitionId());
        instance.setStartTime(LocalDateTime.now());
        instance.setCreateTime(LocalDateTime.now());
        instance.setUpdateTime(LocalDateTime.now());
        instanceMapper.insert(instance);
        return instance;
    }

    private void syncActiveTasks(Long tenantId, AiBpmProcessInstance instance, String engineInstanceId) {
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(engineInstanceId)
                .active()
                .list();
        String currentAssignee = null;
        for (Task engineTask : activeTasks) {
            currentAssignee = engineTask.getAssignee();
            AiBpmTask existing = taskMapper.selectOne(new LambdaQueryWrapper<AiBpmTask>()
                    .eq(AiBpmTask::getTenantId, tenantId)
                    .eq(AiBpmTask::getEngineTaskId, engineTask.getId())
                    .last("limit 1"));
            if (existing != null) {
                continue;
            }
            AiBpmTask task = new AiBpmTask();
            task.setTenantId(tenantId);
            task.setTaskNo("TK-" + shortUuid());
            task.setInstanceId(instance.getId());
            task.setTaskName(engineTask.getName());
            task.setAssignee(engineTask.getAssignee());
            task.setStatus(STATUS_TODO);
            task.setEngineTaskId(engineTask.getId());
            task.setEngineInstanceId(engineInstanceId);
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.insert(task);
        }
        instance.setCurrentAssignee(currentAssignee);
        instance.setUpdateTime(LocalDateTime.now());
        instanceMapper.updateById(instance);
    }

    private String buildSingleApprovalBpmn(BpmDefinitionSaveRequest request) {
        String assignee = StringUtils.hasText(request.getAssigneeExpression())
                ? request.getAssigneeExpression()
                : "${assignee}";
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xmlns:flowable="http://flowable.org/bpmn"
                             targetNamespace="http://aicloud.com/bpm">
                  <process id="%s" name="%s" isExecutable="true">
                    <startEvent id="startEvent" name="开始" />
                    <sequenceFlow id="flow_start_approve" sourceRef="startEvent" targetRef="approveTask" />
                    <userTask id="approveTask" name="%s审批" flowable:assignee="%s" />
                    <sequenceFlow id="flow_approve_end" sourceRef="approveTask" targetRef="endEvent" />
                    <endEvent id="endEvent" name="结束" />
                  </process>
                </definitions>
                """.formatted(request.getProcessKey(), escapeXml(request.getProcessName()), escapeXml(request.getProcessName()), escapeXml(assignee));
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
