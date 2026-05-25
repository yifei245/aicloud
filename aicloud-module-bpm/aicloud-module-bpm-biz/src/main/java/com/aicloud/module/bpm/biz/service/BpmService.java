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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class BpmService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_TODO = "TODO";
    private static final String STATUS_DONE = "DONE";
    private static final String STATUS_FINISHED = "FINISHED";

    private final BpmProcessDefinitionMapper definitionMapper;
    private final BpmProcessInstanceMapper instanceMapper;
    private final BpmTaskMapper taskMapper;

    public BpmService(
            BpmProcessDefinitionMapper definitionMapper,
            BpmProcessInstanceMapper instanceMapper,
            BpmTaskMapper taskMapper) {
        this.definitionMapper = definitionMapper;
        this.instanceMapper = instanceMapper;
        this.taskMapper = taskMapper;
    }

    public List<AiBpmProcessDefinition> definitionList(Long tenantId) {
        return definitionMapper.selectList(new LambdaQueryWrapper<AiBpmProcessDefinition>()
                .eq(AiBpmProcessDefinition::getTenantId, tenantId)
                .orderByDesc(AiBpmProcessDefinition::getId));
    }

    public AiBpmProcessDefinition saveDefinition(BpmDefinitionSaveRequest request) {
        AiBpmProcessDefinition definition = request.getId() == null
                ? new AiBpmProcessDefinition()
                : definitionMapper.selectById(request.getId());
        if (definition == null) {
            throw new IllegalArgumentException("流程定义不存在");
        }

        definition.setTenantId(request.getTenantId());
        definition.setProcessKey(request.getProcessKey());
        definition.setProcessName(request.getProcessName());
        definition.setVersionNo(request.getVersionNo());
        definition.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : STATUS_ACTIVE);
        definition.setCategory(request.getCategory());
        definition.setCreateBy(request.getCreateBy());
        definition.setUpdateTime(LocalDateTime.now());

        if (definition.getId() == null) {
            definition.setCreateTime(LocalDateTime.now());
            definitionMapper.insert(definition);
        } else {
            definitionMapper.updateById(definition);
        }
        return definition;
    }

    public AiBpmProcessInstance start(BpmInstanceStartRequest request) {
        AiBpmProcessDefinition definition = definitionMapper.selectOne(new LambdaQueryWrapper<AiBpmProcessDefinition>()
                .eq(AiBpmProcessDefinition::getTenantId, request.getTenantId())
                .eq(AiBpmProcessDefinition::getProcessKey, request.getProcessKey())
                .eq(AiBpmProcessDefinition::getStatus, STATUS_ACTIVE)
                .orderByDesc(AiBpmProcessDefinition::getVersionNo)
                .last("limit 1"));
        if (definition == null) {
            throw new IllegalArgumentException("有效流程定义不存在");
        }

        AiBpmProcessInstance instance = new AiBpmProcessInstance();
        instance.setTenantId(request.getTenantId());
        instance.setInstanceNo("PI-" + shortUuid());
        instance.setProcessDefinitionId(definition.getId());
        instance.setProcessKey(request.getProcessKey());
        instance.setBusinessId(request.getBusinessId());
        instance.setStarter(request.getStarter());
        instance.setCurrentAssignee(request.getCurrentAssignee());
        instance.setStatus(STATUS_RUNNING);
        instance.setStartTime(LocalDateTime.now());
        instance.setCreateTime(LocalDateTime.now());
        instance.setUpdateTime(LocalDateTime.now());
        instanceMapper.insert(instance);

        AiBpmTask task = new AiBpmTask();
        task.setTenantId(request.getTenantId());
        task.setTaskNo("TK-" + shortUuid());
        task.setInstanceId(instance.getId());
        task.setTaskName(definition.getProcessName() + "审批");
        task.setAssignee(request.getCurrentAssignee());
        task.setStatus(STATUS_TODO);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.insert(task);
        return instance;
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
                .orderByDesc(AiBpmTask::getId));
    }

    public AiBpmTask complete(Long taskId) {
        AiBpmTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        if (!STATUS_TODO.equals(task.getStatus())) {
            throw new IllegalArgumentException("任务不是待办状态");
        }

        task.setStatus(STATUS_DONE);
        task.setCompleteTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        AiBpmProcessInstance instance = instanceMapper.selectById(task.getInstanceId());
        if (instance != null) {
            instance.setStatus(STATUS_FINISHED);
            instance.setEndTime(LocalDateTime.now());
            instance.setUpdateTime(LocalDateTime.now());
            instanceMapper.updateById(instance);
        }
        return task;
    }

    private String shortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
