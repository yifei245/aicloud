package com.aicloud.module.bpm.service.message;

import com.aicloud.framework.common.util.json.JsonUtils;
import com.aicloud.framework.mybatis.core.integration.IntegrationEvent;
import com.aicloud.framework.mybatis.core.integration.IntegrationEventHandler;
import com.aicloud.framework.tenant.core.util.TenantUtils;
import com.aicloud.module.bpm.convert.message.BpmMessageConvert;
import com.aicloud.module.bpm.service.message.dto.BpmNoticeDeliveryDTO;
import com.aicloud.module.system.api.notify.NotifyMessageSendApi;
import com.aicloud.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import com.aicloud.module.system.api.sms.SmsSendApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BpmNoticeDeliveryHandler implements IntegrationEventHandler {
    public static final String TYPE = "BPM_NOTIFICATION";
    @Resource private NotifyMessageSendApi notifyMessageSendApi;
    @Resource private SmsSendApi smsSendApi;
    @Value("${aicloud.bpm.sms-enabled:false}") private boolean smsEnabled;

    @Override public String eventType() { return TYPE; }

    @Override
    public void handle(IntegrationEvent event) {
        BpmNoticeDeliveryDTO notice = JsonUtils.parseObject(event.getPayload(), BpmNoticeDeliveryDTO.class);
        TenantUtils.execute(event.getTenantId(), () -> {
            notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                    .setUserId(notice.getUserId()).setTemplateCode(notice.getTemplateCode())
                    .setTemplateParams(notice.getTemplateParams()).setBusinessKey("bpm:" + event.getBusinessKey())).checkError();
            if (smsEnabled) {
                try {
                    smsSendApi.sendSingleSmsToAdmin(BpmMessageConvert.INSTANCE.convert(notice.getUserId(),
                            notice.getTemplateCode(), notice.getTemplateParams())).checkError();
                } catch (Exception error) {
                    log.warn("Optional BPM SMS failed; in-app notification is stored. eventId={}, template={}", event.getId(), notice.getTemplateCode());
                }
            }
        });
    }
}
