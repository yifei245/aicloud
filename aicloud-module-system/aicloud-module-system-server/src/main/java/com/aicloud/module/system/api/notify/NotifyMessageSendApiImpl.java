package com.aicloud.module.system.api.notify;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.mybatis.core.integration.IntegrationInboxService;
import com.aicloud.framework.tenant.core.context.TenantContextHolder;
import com.aicloud.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import com.aicloud.module.system.service.notify.NotifySendService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static com.aicloud.framework.common.pojo.CommonResult.success;

@RestController // 提供 RESTful API 接口，给 Feign 调用
@Validated
public class NotifyMessageSendApiImpl implements NotifyMessageSendApi {

    @Resource
    private NotifySendService notifySendService;
    @Resource private IntegrationInboxService inbox;

    @Override
    public CommonResult<Long> sendSingleMessageToAdmin(NotifySendSingleToUserReqDTO reqDTO) {
        if (reqDTO.getBusinessKey() == null) return success(notifySendService.sendSingleNotifyToAdmin(reqDTO.getUserId(), reqDTO.getTemplateCode(), reqDTO.getTemplateParams()));
        return success(inbox.execute(TenantContextHolder.getRequiredTenantId(), "notify-admin:" + reqDTO.getUserId(), reqDTO.getBusinessKey(), reqDTO, Long.class,
                () -> notifySendService.sendSingleNotifyToAdmin(reqDTO.getUserId(), reqDTO.getTemplateCode(), reqDTO.getTemplateParams())));
    }

    @Override
    public CommonResult<Long> sendSingleMessageToMember(NotifySendSingleToUserReqDTO reqDTO) {
        if (reqDTO.getBusinessKey() == null) return success(notifySendService.sendSingleNotifyToMember(reqDTO.getUserId(), reqDTO.getTemplateCode(), reqDTO.getTemplateParams()));
        return success(inbox.execute(TenantContextHolder.getRequiredTenantId(), "notify-member:" + reqDTO.getUserId(), reqDTO.getBusinessKey(), reqDTO, Long.class,
                () -> notifySendService.sendSingleNotifyToMember(reqDTO.getUserId(), reqDTO.getTemplateCode(), reqDTO.getTemplateParams())));
    }

}
