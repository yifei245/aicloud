package com.aicloud.auth.mapper;

import com.aicloud.auth.entity.AiSsoSession;
import com.aicloud.auth.model.OnlineSessionResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Mapper
public interface SsoSessionMapper extends BaseMapper<AiSsoSession> {

    /**
     * Mapper operation.
     *
     * @param accessToken accessToken
     * @return operation result
     */
    @Select("""
            SELECT id, tenant_id, user_id, session_id, access_token, refresh_token, login_terminal, expire_time, refresh_expire_time, create_time
            FROM ai_sso_session
            WHERE access_token = #{accessToken}
            LIMIT 1
            """)
    AiSsoSession findByAccessToken(@Param("accessToken") String accessToken);

    /**
     * Mapper operation.
     *
     * @param refreshToken refreshToken
     * @return operation result
     */
    @Select("""
            SELECT id, tenant_id, user_id, session_id, access_token, refresh_token, login_terminal, expire_time, refresh_expire_time, create_time
            FROM ai_sso_session
            WHERE refresh_token = #{refreshToken}
            LIMIT 1
            """)
    AiSsoSession findByRefreshToken(@Param("refreshToken") String refreshToken);

    /**
     * Mapper operation.
     *
     * @param tenantId tenantId
     * @param userId userId
     * @return operation result
     */
    @Delete("""
            DELETE FROM ai_sso_session
            WHERE tenant_id = #{tenantId} AND user_id = #{userId}
            """)
    int deleteByTenantAndUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    /**
     * Mapper operation.
     *
     * @param id id
     * @param refreshToken refreshToken
     * @return operation result
     */
    @Delete("""
            DELETE FROM ai_sso_session
            WHERE id = #{id} AND refresh_token = #{refreshToken}
            """)
    int deleteByIdAndRefreshToken(@Param("id") Long id, @Param("refreshToken") String refreshToken);

    /**
     * Mapper operation.
     *
     * @param sessionId sessionId
     * @param tenantId tenantId
     * @return operation result
     */
    @Delete("""
            DELETE FROM ai_sso_session
            WHERE tenant_id = #{tenantId} AND session_id = #{sessionId}
            """)
    int deleteByTenantAndSessionId(@Param("tenantId") Long tenantId, @Param("sessionId") String sessionId);

    /**
     * Mapper operation.
     *
     * @param tenantId tenantId
     * @param userId userId
     * @return operation result
     */
    @Select("""
            SELECT
              s.session_id AS sessionId,
              s.tenant_id AS tenantId,
              s.user_id AS userId,
              u.username AS username,
              u.user_type AS userType,
              s.login_terminal AS loginTerminal,
              s.expire_time AS expireAt,
              s.create_time AS createTime
            FROM ai_sso_session s
            INNER JOIN ai_user u ON u.id = s.user_id AND u.tenant_id = s.tenant_id
            WHERE s.tenant_id = #{tenantId}
              AND (#{userId} IS NULL OR s.user_id = #{userId})
            ORDER BY s.create_time DESC
            """)
    List<OnlineSessionResponse> listOnlineSessions(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
