package com.aicloud.auth.service;

import com.aicloud.auth.entity.AiOauth2Client;
import com.aicloud.auth.entity.AiLoginLog;
import com.aicloud.auth.entity.AiSsoSession;
import com.aicloud.auth.entity.AiUser;
import com.aicloud.auth.mapper.LoginLogMapper;
import com.aicloud.auth.mapper.Oauth2ClientMapper;
import com.aicloud.auth.mapper.SsoSessionMapper;
import com.aicloud.auth.mapper.UserMapper;
import com.aicloud.auth.model.OnlineSessionResponse;
import com.aicloud.auth.model.TokenResponse;
import com.aicloud.auth.model.TokenVerifyResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class TokenStoreService {

    private static final Long DEFAULT_TENANT_ID = 1L;
    private static final long ACCESS_TOKEN_SECONDS = 2 * 60 * 60;
    private static final long REFRESH_TOKEN_SECONDS = 30L * 24 * 60 * 60;
    private final UserMapper userMapper;
    private final Oauth2ClientMapper oauth2ClientMapper;
    private final SsoSessionMapper ssoSessionMapper;
    private final LoginLogMapper loginLogMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public TokenStoreService(UserMapper userMapper, Oauth2ClientMapper oauth2ClientMapper, SsoSessionMapper ssoSessionMapper,
                             LoginLogMapper loginLogMapper) {
        this.userMapper = userMapper;
        this.oauth2ClientMapper = oauth2ClientMapper;
        this.ssoSessionMapper = ssoSessionMapper;
        this.loginLogMapper = loginLogMapper;
    }

    public TokenResponse login(Long tenantId, String username, String password, String terminal, String loginIp) {
        Long actualTenantId = normalizeTenantId(tenantId);
        AiUser user = userMapper.findByUsername(actualTenantId, username);
        if (user == null || !isPasswordMatched(password, user.getPassword())) {
            recordLogin(actualTenantId, null, username, null, terminal, loginIp, 0, "用户名或密码错误");
            return null;
        }

        List<String> roleCodes = userMapper.listRoleCodes(actualTenantId, user.getId());
        List<String> permissions = userMapper.listPermissions(actualTenantId, user.getId());
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        recordLogin(actualTenantId, user.getId(), user.getUsername(), user.getUserType(), terminal, loginIp, 1, null);
        return createSession(actualTenantId, user, roleCodes, permissions, terminal);
    }

    public boolean verifyClient(Long tenantId, String clientId, String clientSecret) {
        Long actualTenantId = normalizeTenantId(tenantId);
        AiOauth2Client client = oauth2ClientMapper.findByClientId(actualTenantId, clientId);
        return client != null && StringUtils.hasText(clientSecret) && clientSecret.equals(client.getClientSecret());
    }

    public TokenVerifyResponse verify(String token) {
        TokenVerifyResponse response = new TokenVerifyResponse();
        AiSsoSession session = ssoSessionMapper.findByAccessToken(token);
        if (session == null || session.getExpireTime() == null || session.getExpireTime().isBefore(LocalDateTime.now())) {
            response.setValid(false);
            return response;
        }
        AiUser user = userMapper.selectById(session.getUserId());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            response.setValid(false);
            return response;
        }
        List<String> roleCodes = userMapper.listRoleCodes(session.getTenantId(), user.getId());
        List<String> permissions = userMapper.listPermissions(session.getTenantId(), user.getId());
        response.setValid(true);
        response.setTenantId(session.getTenantId());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setUserType(user.getUserType());
        response.setLoginTerminal(session.getLoginTerminal());
        response.setRoles(roleCodes);
        response.setPermissions(permissions);
        response.setExpireAt(session.getExpireTime());
        return response;
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return null;
        }
        AiSsoSession oldSession = ssoSessionMapper.findByRefreshToken(refreshToken);
        if (oldSession == null || oldSession.getRefreshExpireTime() == null || oldSession.getRefreshExpireTime().isBefore(LocalDateTime.now())) {
            return null;
        }
        int deleted = ssoSessionMapper.deleteByIdAndRefreshToken(oldSession.getId(), refreshToken);
        if (deleted <= 0) {
            return null;
        }
        AiUser user = userMapper.selectById(oldSession.getUserId());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return null;
        }
        List<String> roleCodes = userMapper.listRoleCodes(oldSession.getTenantId(), user.getId());
        List<String> permissions = userMapper.listPermissions(oldSession.getTenantId(), user.getId());
        return createSession(oldSession.getTenantId(), user, roleCodes, permissions, oldSession.getLoginTerminal());
    }

    public int kickoutUserSessions(Long tenantId, Long userId) {
        Long actualTenantId = normalizeTenantId(tenantId);
        return ssoSessionMapper.deleteByTenantAndUserId(actualTenantId, userId);
    }

    public int kickoutSession(Long tenantId, String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return 0;
        }
        Long actualTenantId = normalizeTenantId(tenantId);
        return ssoSessionMapper.deleteByTenantAndSessionId(actualTenantId, sessionId);
    }

    public List<OnlineSessionResponse> listOnlineSessions(Long tenantId, Long userId) {
        Long actualTenantId = normalizeTenantId(tenantId);
        return ssoSessionMapper.listOnlineSessions(actualTenantId, userId);
    }

    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        AiSsoSession session = ssoSessionMapper.findByAccessToken(token);
        if (session != null) {
            ssoSessionMapper.deleteById(session.getId());
        }
    }

    private boolean isPasswordMatched(String rawPassword, String dbPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(dbPassword)) {
            return false;
        }
        if (rawPassword.equals(dbPassword)) {
            return true;
        }
        if (dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$") || dbPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, dbPassword);
        }
        return false;
    }

    private Long normalizeTenantId(Long tenantId) {
        return tenantId == null ? DEFAULT_TENANT_ID : tenantId;
    }

    private TokenResponse createSession(Long tenantId, AiUser user, List<String> roleCodes, List<String> permissions, String terminal) {
        LocalDateTime expireAt = LocalDateTime.now().plusSeconds(ACCESS_TOKEN_SECONDS);
        LocalDateTime refreshExpireAt = LocalDateTime.now().plusSeconds(REFRESH_TOKEN_SECONDS);
        String accessToken = "atk_" + UUID.randomUUID().toString().replace("-", "");
        String refreshToken = "rtk_" + UUID.randomUUID().toString().replace("-", "");
        String sessionId = "sid_" + UUID.randomUUID().toString().replace("-", "");

        AiSsoSession session = new AiSsoSession();
        session.setTenantId(tenantId);
        session.setUserId(user.getId());
        session.setSessionId(sessionId);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setLoginTerminal(StringUtils.hasText(terminal) ? terminal : "WEB");
        session.setExpireTime(expireAt);
        session.setRefreshExpireTime(refreshExpireAt);
        session.setCreateTime(LocalDateTime.now());
        ssoSessionMapper.insert(session);

        TokenResponse response = new TokenResponse();
        response.setTokenType("Bearer");
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setExpireAt(expireAt);
        response.setRefreshExpireAt(refreshExpireAt);
        response.setExpiresIn(ACCESS_TOKEN_SECONDS);
        response.setTenantId(tenantId);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setUserType(user.getUserType());
        response.setRoles(roleCodes);
        response.setPermissions(permissions);
        return response;
    }

    private void recordLogin(Long tenantId, Long userId, String username, String userType,
                             String terminal, String loginIp, Integer success, String errorMsg) {
        AiLoginLog log = new AiLoginLog();
        log.setTenantId(tenantId);
        log.setUserId(userId);
        log.setUsername(username);
        log.setUserType(userType);
        log.setLoginTerminal(StringUtils.hasText(terminal) ? terminal : "WEB");
        log.setLoginIp(loginIp);
        log.setSuccess(success);
        log.setErrorMsg(errorMsg);
        log.setCreateTime(LocalDateTime.now());
        loginLogMapper.insert(log);
    }
}
