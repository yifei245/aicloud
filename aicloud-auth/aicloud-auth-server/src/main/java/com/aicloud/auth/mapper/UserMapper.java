package com.aicloud.auth.mapper;

import com.aicloud.auth.entity.AiUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Mapper
public interface UserMapper extends BaseMapper<AiUser> {

    /**
     * Mapper operation.
     *
     * @param tenantId tenantId
     * @param username username
     * @return operation result
     */
    @Select("""
            SELECT id, tenant_id, username, password, user_type, status
            FROM ai_user
            WHERE tenant_id = #{tenantId} AND username = #{username} AND status = 1
            LIMIT 1
            """)
    AiUser findByUsername(@Param("tenantId") Long tenantId, @Param("username") String username);

    /**
     * Mapper operation.
     *
     * @param tenantId tenantId
     * @param userId userId
     * @return operation result
     */
    @Select("""
            SELECT r.code
            FROM ai_role r
            INNER JOIN ai_user_role ur ON ur.role_id = r.id AND ur.tenant_id = r.tenant_id
            WHERE ur.tenant_id = #{tenantId} AND ur.user_id = #{userId} AND r.status = 1
            ORDER BY r.id
            """)
    List<String> listRoleCodes(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    /**
     * Mapper operation.
     *
     * @param tenantId tenantId
     * @param userId userId
     * @return operation result
     */
    @Select("""
            SELECT DISTINCT m.permission
            FROM ai_menu m
            INNER JOIN ai_role_menu rm ON rm.menu_id = m.id AND rm.tenant_id = m.tenant_id
            INNER JOIN ai_user_role ur ON ur.role_id = rm.role_id AND ur.tenant_id = rm.tenant_id
            WHERE ur.tenant_id = #{tenantId}
              AND ur.user_id = #{userId}
              AND m.status = 1
              AND m.type = 3
              AND m.permission IS NOT NULL
              AND m.permission != ''
            """)
    List<String> listPermissions(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
}
