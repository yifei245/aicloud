package com.aicloud.module.bpm.service.message;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.mybatis.core.integration.IntegrationEvent;
import com.aicloud.module.bpm.service.message.dto.BpmNoticeDeliveryDTO;
import com.aicloud.module.system.api.notify.NotifyMessageSendApi;
import com.aicloud.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import com.aicloud.module.system.api.sms.SmsSendApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BpmNoticeDeliveryHandlerTest {
    @Mock NotifyMessageSendApi notifyMessageSendApi;
    @Mock SmsSendApi smsSendApi;
    @InjectMocks BpmNoticeDeliveryHandler handler;

    @Test void eachApprovalStageHasInAppNotificationWithSmsDisabled() {
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any())).thenReturn(CommonResult.success(1L));
        for (String template : new String[]{"bpm_task_assigned", "bpm_process_instance_approve", "bpm_process_instance_reject", "bpm_task_timeout"}) {
            handler.handle(event(template));
        }
        var captured = org.mockito.ArgumentCaptor.forClass(NotifySendSingleToUserReqDTO.class);
        verify(notifyMessageSendApi, times(4)).sendSingleMessageToAdmin(captured.capture());
        assertTrue(captured.getAllValues().stream().allMatch(n -> n.getUserId() == 7L && n.getBusinessKey().startsWith("bpm:")));
        verifyNoInteractions(smsSendApi);
    }

    @Test void optionalSmsFailureDoesNotRepeatOrUndoInAppDelivery() {
        ReflectionTestUtils.setField(handler, "smsEnabled", true);
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any())).thenReturn(CommonResult.success(1L));
        when(smsSendApi.sendSingleSmsToAdmin(any())).thenThrow(new IllegalStateException("Channel unavailable"));
        assertDoesNotThrow(() -> handler.handle(event("bpm_task_assigned")));
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(any());
    }

    @Test void inAppFailurePropagatesForDurableOutboxRetry() {
        when(notifyMessageSendApi.sendSingleMessageToAdmin(any())).thenReturn(CommonResult.error(503, "Unavailable"));
        assertThrows(com.aicloud.framework.common.exception.ServiceException.class, () -> handler.handle(event("bpm_task_assigned")));
        verifyNoInteractions(smsSendApi);
    }

    private IntegrationEvent event(String template) {
        return new IntegrationEvent().setId(1L).setTenantId(1L).setBusinessKey(template + ":task:7")
                .setPayload(JsonUtils.toJsonString(new BpmNoticeDeliveryDTO().setUserId(7L).setTemplateCode(template)
                        .setTemplateParams(Map.of("processInstanceName", "Verification", "detailUrl", "/bpm/instance/1"))));
    }
}
