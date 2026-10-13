package com.aicloud.module.system.controller.admin.oauth2;

import com.aicloud.framework.common.exception.ServiceException;
import com.aicloud.framework.common.enums.UserTypeEnum;
import com.aicloud.framework.security.core.LoginUser;
import com.aicloud.module.system.dal.dataobject.oauth2.OAuth2ClientDO;
import com.aicloud.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.aicloud.module.system.service.oauth2.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2OpenControllerTest {
    @InjectMocks OAuth2OpenController controller;
    @Mock OAuth2GrantService grants;
    @Mock OAuth2ClientService clients;
    @Mock OAuth2ApproveService approvals;
    @Mock OAuth2TokenService tokens;
    MockHttpServletRequest request;

    @BeforeEach void setup() {
        request = new MockHttpServletRequest("POST", "/admin-api/system/oauth2/token");
        request.setParameter("client_id", "test");
        request.setParameter("client_secret", "secret");
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    OAuth2ClientDO client(String secret) { return new OAuth2ClientDO().setClientId("test").setSecret(secret); }
    void mockClient(String secret) {
        when(clients.validOAuthClientFromCache(eq("test"),any(),any(),any(),any())).thenReturn(client(secret));
    }
    void login(int type) {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(
                new LoginUser().setId(1L).setUserType(type).setTenantId(1L),null));
    }
    @Test void confidentialClientCannotOmitSecret() {
        request.removeParameter("client_secret"); mockClient("secret");
        assertThrows(ServiceException.class,()->controller.postAccessToken(request,"authorization_code",
                "code","http://localhost/cb","state",null,null,null,null,null));
        verifyNoInteractions(grants);
    }
    @Test void confidentialClientCannotUseWrongSecret() {
        mockClient("other-secret");
        assertThrows(ServiceException.class,()->controller.postAccessToken(request,"authorization_code",
                "code","http://localhost/cb","state",null,null,null,null,null));
        verifyNoInteractions(grants);
    }
    @Test void publicClientMustUsePkceGrant() {
        request.removeParameter("client_secret");mockClient("");
        when(grants.grantAuthorizationCodeForAccessToken("test","code","http://localhost/cb",
                "state","verifier",true)).thenReturn(new OAuth2AccessTokenDO().setAccessToken("token")
                .setScopes(List.of("user.read")).setExpiresTime(LocalDateTime.now().plusMinutes(10)));
        assertEquals(0,controller.postAccessToken(request,"authorization_code","code","http://localhost/cb",
                "state",null,null,null,null,"verifier").getCode());
    }
    @Test void publicClientCannotUseClientCredentials() {
        request.removeParameter("client_secret");mockClient("");
        assertThrows(ServiceException.class,()->controller.postAccessToken(request,"client_credentials",
                null,null,null,null,null,null,null,null));verifyNoInteractions(grants);
    }
    @Test void implicitIsRejected() {
        assertThrows(ServiceException.class,()->controller.postAccessToken(request,"implicit",
                null,null,null,null,null,null,null,null));verifyNoInteractions(grants);
        assertThrows(ServiceException.class,()->controller.approveOrDeny("token","test","{}",
                "http://localhost/cb",false,"state",null,null));
    }
    @Test void publicAuthorizationRequiresS256() {
        when(clients.validOAuthClientFromCache(eq("test"),isNull(),eq("authorization_code"),any(),any()))
                .thenReturn(client(""));
        assertThrows(ServiceException.class,()->controller.approveOrDeny("code","test","{}",
                "http://localhost/cb",false,"state",null,null));verifyNoInteractions(grants);
    }
    @Test void globalLogoutRevokesAllClientsForCurrentIdentity() {
        login(UserTypeEnum.MEMBER.getValue());
        assertTrue(controller.logoutAll().getData());
        verify(tokens).removeAccessToken(1L,UserTypeEnum.MEMBER.getValue());
    }
    @Test void authorizeUsesActualMemberIdentity() {
        login(UserTypeEnum.MEMBER.getValue());
        when(clients.validOAuthClientFromCache("test")).thenReturn(client("secret").setScopes(List.of("user.read")));
        when(approvals.getApproveList(1L,UserTypeEnum.MEMBER.getValue(),"test")).thenReturn(List.of());
        assertEquals(0,controller.authorize("test").getCode());
        verify(approvals).getApproveList(1L,UserTypeEnum.MEMBER.getValue(),"test");
    }
}
