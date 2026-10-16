package com.aicloud.module.bpm.api.event;
import com.aicloud.module.bpm.framework.flowable.core.util.BpmHttpRequestUtils;
import jakarta.annotation.Resource;
import org.springframework.web.client.RestTemplate;
public class HrmLeaveStatusListener extends BpmProcessInstanceStatusEventListener {
    @Resource RestTemplate loadBalancedRestTemplate;
    @Override protected String getProcessDefinitionKey() { return "hrm_attendance_leave"; }
    @Override protected void onEvent(BpmProcessInstanceStatusEvent event) {
        BpmHttpRequestUtils.executeBpmHttpRequest(event,"http://hrm-server/rpc-api/hrm/leave/process-status",loadBalancedRestTemplate);
    }
}
