package com.aicloud.module.bpm.framework.flowable.core.util;

import com.aicloud.framework.common.exception.ServiceException;
import cn.hutool.extra.spring.SpringUtil;
import com.aicloud.framework.security.config.SecurityProperties;
import com.aicloud.module.bpm.api.event.BpmProcessInstanceStatusEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BpmHttpRequestUtilsTest {

    private BpmProcessInstanceStatusEvent event() {
        return new BpmProcessInstanceStatusEvent(new Object()).setId("process-1")
                .setProcessDefinitionKey("oa_leave_apply").setBusinessKey("7").setStatus(2).setTenantId("1");
    }

    @Test
    void internalCallbackCarriesCredentialAndTenantWithoutApplicationSource() {
        RestTemplate client = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(client).build();
        String url = "http://oa-server/rpc-api/oa/leave-apply/update-audit-status";
        server.expect(requestTo(url))
                .andExpect(header(SecurityProperties.INTERNAL_TOKEN_HEADER, "unit-test-credential"))
                .andExpect(header("tenant-id", "1"))
                .andExpect(jsonPath("$.businessKey").value("7"))
                .andExpect(jsonPath("$.source").doesNotExist())
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));
        try (MockedStatic<SpringUtil> spring = mockStatic(SpringUtil.class)) {
            SecurityProperties properties = new SecurityProperties();
            properties.setInternalSecret("unit-test-credential");
            spring.when(() -> SpringUtil.getBean(SecurityProperties.class)).thenReturn(properties);
            BpmHttpRequestUtils.executeBpmHttpRequest(event(), url, client);
        }
        server.verify();
    }

    @Test
    void externalNotificationNeverCarriesInternalCredential() {
        RestTemplate client = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(client).build();
        String url = "https://example.invalid/rpc-api/oa/notify";
        server.expect(requestTo(url))
                .andExpect(headerDoesNotExist(SecurityProperties.INTERNAL_TOKEN_HEADER))
                .andRespond(withSuccess("{\"code\":0}", MediaType.APPLICATION_JSON));
        BpmHttpRequestUtils.executeBpmHttpRequest(event(), url, client);
        server.verify();
    }

    @Test
    void businessFailureWithHttp200FailsApprovalCallback() {
        RestTemplate client = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(client).build();
        String url = "https://example.invalid/notify";
        server.expect(requestTo(url)).andRespond(withSuccess("{\"code\":500,\"msg\":\"failed\"}", MediaType.APPLICATION_JSON));
        assertThrows(ServiceException.class, () -> BpmHttpRequestUtils.executeBpmHttpRequest(event(), url, client));
        server.verify();
    }
}
