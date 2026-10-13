package com.aicloud.module.system.service.oauth2;

import com.aicloud.framework.common.exception.ServiceException;
import com.aicloud.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import com.aicloud.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2PkceTest {
    @InjectMocks OAuth2GrantServiceImpl service;
    @Mock OAuth2CodeService codes;
    @Mock OAuth2TokenService tokens;
    static final String VERIFIER="dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
    static final String CHALLENGE="E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";
    OAuth2CodeDO code() { return new OAuth2CodeDO().setClientId("public").setUserId(1L).setUserType(2)
            .setRedirectUri("http://localhost/cb").setState("state").setScopes(List.of("user.read"))
            .setCodeChallenge(CHALLENGE); }
    @Test void rfc7636S256VectorIsAccepted() {
        when(codes.consumeAuthorizationCode("code")).thenReturn(code());
        when(tokens.createAccessToken(1L,2,"public",List.of("user.read"))).thenReturn(new OAuth2AccessTokenDO());
        assertNotNull(service.grantAuthorizationCodeForAccessToken("public","code","http://localhost/cb","state",VERIFIER,true));
    }
    @Test void wrongVerifierNeverIssuesToken() {
        when(codes.consumeAuthorizationCode("code")).thenReturn(code());
        assertThrows(ServiceException.class,()->service.grantAuthorizationCodeForAccessToken("public","code",
                "http://localhost/cb","state","a".repeat(43),true));verifyNoInteractions(tokens);
    }
    @Test void publicClientCannotRedeemCodeWithoutChallenge() {
        when(codes.consumeAuthorizationCode("code")).thenReturn(code().setCodeChallenge(null));
        assertThrows(ServiceException.class,()->service.grantAuthorizationCodeForAccessToken("public","code",
                "http://localhost/cb","state",VERIFIER,true));verifyNoInteractions(tokens);
    }
}
