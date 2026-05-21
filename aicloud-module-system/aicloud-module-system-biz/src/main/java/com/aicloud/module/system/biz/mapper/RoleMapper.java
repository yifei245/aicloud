package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RoleMapper extends BaseMapper<AiRole> {

    @Select("""
            SELECT r.id, r.code, r.name, r.data_scope, r.sort, r.status, r.create_time,
                   COUNT(ur.id) AS user_count
            FROM ai_role r
            LEFT JOIN ai_user_role ur ON ur.role_id = r.id AND ur.tenant_id = r.tenant_id
            WHERE r.tenant_id = #{tenantId}
              AND (#{keyword} IS NULL OR #{keyword} = '' OR r.code LIKE CONCAT('%', #{keyword}, '%') OR r.name LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{status} IS NULL OR r.status = #{status})
            GROUP BY r.id, r.code, r.name, r.data_scope, r.sort, r.status, r.create_time
            ORDER BY r.sort ASC, r.id ASC
            """)
    List<Map<String, Object>> listRoleRows(@Param("tenantId") Long tenantId,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status);

    @Select("SELECT menu_id FROM ai_role_menu WHERE tenant_id = #{tenantId} AND role_id = #{roleId} ORDER BY menu_id")
    List<Long> listMenuIds(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);

    @Select("SELECT COUNT(1) FROM ai_role WHERE tenant_id = #{tenantId} AND code = #{code} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    long countByCode(@Param("tenantId") Long tenantId, @Param("code") String code, @Param("excludeId") Long excludeId);
}
