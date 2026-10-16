package com.aicloud.module.bpm.service.message;

import com.aicloud.framework.web.config.WebProperties;
import com.aicloud.framework.mybatis.core.integration.IntegrationOutboxService;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.bpm.enums.message.BpmMessageEnum;
import com.aicloud.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceApproveReqDTO;
import com.aicloud.module.bpm.service.message.dto.BpmMessageSendWhenProcessInstanceRejectReqDTO;
import com.aicloud.module.bpm.service.message.dto.BpmMessageSendWhenTaskCreatedReqDTO;
import com.aicloud.module.bpm.service.message.dto.BpmMessageSendWhenTaskTimeoutReqDTO;
import com.aicloud.module.bpm.service.message.dto.BpmNoticeDeliveryDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

/**
 * BPM 消息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class BpmMessageServiceImpl implements BpmMessageService {

    @Resource
    private IntegrationOutboxService outboxService;

    @Resource
    private WebProperties webProperties;

    @Override
    public void sendMessageWhenProcessInstanceApprove(BpmMessageSendWhenProcessInstanceApproveReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        enqueue(BpmMessageEnum.PROCESS_INSTANCE_APPROVE, reqDTO.getProcessInstanceId(), reqDTO.getStartUserId(), templateParams, false);
    }

    @Override
    public void sendMessageWhenProcessInstanceReject(BpmMessageSendWhenProcessInstanceRejectReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("reason", reqDTO.getReason() == null ? "" : reqDTO.getReason());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        enqueue(BpmMessageEnum.PROCESS_INSTANCE_REJECT, reqDTO.getProcessInstanceId(), reqDTO.getStartUserId(), templateParams, false);
    }

    @Override
    public void sendMessageWhenTaskAssigned(BpmMessageSendWhenTaskCreatedReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("taskName", reqDTO.getTaskName());
        templateParams.put("startUserNickname", reqDTO.getStartUserNickname());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        // Flowable assignment handler invokes this after the task transaction completes.
        enqueue(BpmMessageEnum.TASK_ASSIGNED, reqDTO.getTaskId(), reqDTO.getAssigneeUserId(), templateParams, true);
    }

    @Override
    public void sendMessageWhenTaskTimeout(BpmMessageSendWhenTaskTimeoutReqDTO reqDTO) {
        Map<String, Object> templateParams = new HashMap<>();
        templateParams.put("processInstanceName", reqDTO.getProcessInstanceName());
        templateParams.put("taskName", reqDTO.getTaskName());
        templateParams.put("detailUrl", getProcessInstanceDetailUrl(reqDTO.getProcessInstanceId()));
        enqueue(BpmMessageEnum.TASK_TIMEOUT, reqDTO.getTaskId(), reqDTO.getAssigneeUserId(), templateParams, false);
    }

    private void enqueue(BpmMessageEnum type, String sourceId, Long userId, Map<String, Object> params, boolean afterCompletion) {
        String key = type.name() + ":" + sourceId + ":" + userId;
        BpmNoticeDeliveryDTO body = new BpmNoticeDeliveryDTO().setUserId(userId)
                .setTemplateCode(type.getSmsTemplateCode()).setTemplateParams(params);
        if (afterCompletion) outboxService.enqueueIndependent(TenantContextHolder.getRequiredTenantId(), BpmNoticeDeliveryHandler.TYPE, key, body);
        else outboxService.enqueue(TenantContextHolder.getRequiredTenantId(), BpmNoticeDeliveryHandler.TYPE, key, body);
    }

    private String getProcessInstanceDetailUrl(String taskId) {
        return webProperties.getAdminUi().getUrl() + "/bpm/process-instance/detail?id=" + taskId;
    }

}
