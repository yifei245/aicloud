package com.aicloud.auth.mapper;

import com.aicloud.auth.entity.AiOauth2Client;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface Oauth2ClientMapper extends BaseMapper<AiOauth2Client> {

    @Select("""
            SELECT id, tenant_id, client_id, client_secret, status
            FROM ai_oauth2_client
            WHERE tenant_id = #{tenantId} AND client_id = #{clientId} AND status = 1
            LIMIT 1
            """)
    AiOauth2Client findByClientId(@Param("tenantId") Long tenantId, @Param("clientId") String clientId);
}
