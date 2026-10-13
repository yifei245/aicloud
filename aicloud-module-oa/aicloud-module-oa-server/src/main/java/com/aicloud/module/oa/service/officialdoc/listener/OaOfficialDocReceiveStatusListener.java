package com.aicloud.module.oa.service.officialdoc.listener;

import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEventListener;
import com.aicloud.module.oa.enums.BpmModelConstants;
import com.aicloud.module.oa.service.officialdoc.OaOfficialDocReceiveService;
import jakarta.annotation.Resource;
import com.aicloud.module.oa.enums.ApiConstants;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公文收文审批结果监听器
 *
 * @author 芋道源码
 */
@RestController
@Validated
public class OaOfficialDocReceiveStatusListener extends BpmProcessInstanceStatusEventListener {

    @Resource
    private OaOfficialDocReceiveService officialDocReceiveService;

    @Override
    public String getProcessDefinitionKey() {
        return BpmModelConstants.OFFICIAL_DOC_RECEIVE;
    }

    @Override
    @PostMapping(ApiConstants.PREFIX + "/official-doc-receive/update-audit-status") // 提供给 bpm-server RPC 调用
    protected void onEvent(@RequestBody BpmProcessInstanceStatusEvent event) {
        officialDocReceiveService.updateOfficialDocReceiveStatus(Long.parseLong(event.getBusinessKey()), event.getStatus());
    }

}
