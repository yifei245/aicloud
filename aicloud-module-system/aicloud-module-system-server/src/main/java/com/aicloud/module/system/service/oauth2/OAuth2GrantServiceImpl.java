package com.aicloud.module.system.service.oauth2;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.aicloud.framework.common.enums.UserTypeEnum;
import com.aicloud.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.aicloud.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import com.aicloud.module.system.dal.dataobject.user.AdminUserDO;
import com.aicloud.module.system.enums.ErrorCodeConstants;
import com.aicloud.module.system.service.auth.AdminAuthService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.aicloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.aicloud.framework.common.exception.util.ServiceExceptionUtil.exception0;

/**
 * OAuth2 授予 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class OAuth2GrantServiceImpl implements OAuth2GrantService {

    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private OAuth2CodeService oauth2CodeService;
    @Resource
    private AdminAuthService adminAuthService;

    @Override
    public OAuth2AccessTokenDO grantImplicit(Long userId, Integer userType,
                                             String clientId, List<String> scopes) {
        return oauth2TokenService.createAccessToken(userId, userType, clientId, scopes);
    }

    @Override
    public String grantAuthorizationCodeForCode(Long userId, Integer userType,
                                                String clientId, List<String> scopes,
                                                String redirectUri, String state) {
        return oauth2CodeService.createAuthorizationCode(userId, userType, clientId, scopes,
                redirectUri, state).getCode();
    }

    @Override
    public OAuth2AccessTokenDO grantAuthorizationCodeForAccessToken(String clientId, String code,
                                                                    String redirectUri, String state) {
        return grantAuthorizationCodeForAccessToken(clientId, code, redirectUri, state, null, false);
    }

    @Override
    public String grantAuthorizationCodeForCode(Long userId, Integer userType, String clientId,
            List<String> scopes, String redirectUri, String state, String codeChallenge) {
        return oauth2CodeService.createAuthorizationCode(userId, userType, clientId, scopes,
                redirectUri, state, codeChallenge).getCode();
    }

    @Override
    public OAuth2AccessTokenDO grantAuthorizationCodeForAccessToken(String clientId, String code,
            String redirectUri, String state, String codeVerifier, boolean requirePkce) {
        OAuth2CodeDO codeDO = oauth2CodeService.consumeAuthorizationCode(code);
        Assert.notNull(codeDO, "授权码不能为空"); // 防御性编程
        // 校验 clientId 是否匹配
        if (!StrUtil.equals(clientId, codeDO.getClientId())) {
            throw exception(ErrorCodeConstants.OAUTH2_GRANT_CLIENT_ID_MISMATCH);
        }
        // 校验 redirectUri 是否匹配
        if (!StrUtil.equals(redirectUri, codeDO.getRedirectUri())) {
            throw exception(ErrorCodeConstants.OAUTH2_GRANT_REDIRECT_URI_MISMATCH);
        }
        // 校验 state 是否匹配
        state = StrUtil.nullToDefault(state, ""); // 数据库 state 为 null 时，会设置为 "" 空串
        if (!StrUtil.equals(state, codeDO.getState())) {
            throw exception(ErrorCodeConstants.OAUTH2_GRANT_STATE_MISMATCH);
        }

        if (requirePkce || StrUtil.isNotBlank(codeDO.getCodeChallenge())) {
            if (codeVerifier == null || !codeVerifier.matches("[A-Za-z0-9._~-]{43,128}")
                    || StrUtil.isBlank(codeDO.getCodeChallenge())) {
                throw exception0(400, "缺少或无效的 PKCE code_verifier");
            }
            try {
                String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
                        MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII)));
                if (!MessageDigest.isEqual(challenge.getBytes(StandardCharsets.US_ASCII),
                        codeDO.getCodeChallenge().getBytes(StandardCharsets.US_ASCII))) {
                    throw exception0(400, "PKCE 校验失败");
                }
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalStateException("JDK SHA-256 unavailable", ex);
            }
        }

        // 创建访问令牌
        return oauth2TokenService.createAccessToken(codeDO.getUserId(), codeDO.getUserType(),
                codeDO.getClientId(), codeDO.getScopes());
    }

    @Override
    public OAuth2AccessTokenDO grantPassword(String username, String password, String clientId, List<String> scopes) {
        // 使用账号 + 密码进行登录
        AdminUserDO user = adminAuthService.authenticate(username, password);
        Assert.notNull(user, "用户不能为空！"); // 防御性编程

        // 创建访问令牌
        return oauth2TokenService.createAccessToken(user.getId(), UserTypeEnum.ADMIN.getValue(), clientId, scopes);
    }

    @Override
    public OAuth2AccessTokenDO grantRefreshToken(String refreshToken, String clientId) {
        return oauth2TokenService.refreshAccessToken(refreshToken, clientId);
    }

    @Override
    public OAuth2AccessTokenDO grantClientCredentials(String clientId, List<String> scopes) {
        // 特殊：https://yuanbao.tencent.com/bot/app/share/chat/wFj642xSZHHx
        return oauth2TokenService.createAccessToken(0L, UserTypeEnum.APPLICATION.getValue(), clientId, scopes);
    }

    @Override
    public boolean revokeToken(String clientId, String accessToken) {
        // 先查询，保证 clientId 时匹配的
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.getAccessToken(accessToken);
        if (accessTokenDO == null || ObjectUtil.notEqual(clientId, accessTokenDO.getClientId())) {
            return false;
        }
        // 再删除
        return oauth2TokenService.removeAccessToken(accessToken) != null;
    }

}
