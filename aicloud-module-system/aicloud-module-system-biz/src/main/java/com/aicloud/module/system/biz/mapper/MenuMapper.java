package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiMenu;
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
public interface MenuMapper extends BaseMapper<AiMenu> {

        /**
     * Lists menu entries visible to a user.
     *
     * @param tenantId tenant id
     * @param userId user id
     * @return menu list
     */
@Select("""
            SELECT DISTINCT m.id, m.tenant_id, m.parent_id, m.name, m.type, m.path, m.component, m.permission, m.icon, m.visible, m.sort, m.status
            FROM ai_menu m
            INNER JOIN ai_role_menu rm ON rm.menu_id = m.id AND rm.tenant_id = m.tenant_id
            INNER JOIN ai_user_role ur ON ur.role_id = rm.role_id AND ur.tenant_id = rm.tenant_id
            WHERE ur.tenant_id = #{tenantId} AND ur.user_id = #{userId} AND m.status = 1
            ORDER BY m.sort ASC, m.id ASC
            """)
    List<AiMenu> listMenusByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

        /**
     * Lists button permissions visible to a user.
     *
     * @param tenantId tenant id
     * @param userId user id
     * @return permission codes
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
    List<String> listButtonPermissionsByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

        /**
     * Lists all tenant menus.
     *
     * @param tenantId tenant id
     * @return menu list
     */
@Select("""
            SELECT id, tenant_id, parent_id, name, type, path, component, permission, icon, visible, sort, status
            FROM ai_menu
            WHERE tenant_id = #{tenantId}
            ORDER BY sort ASC, id ASC
            """)
    List<AiMenu> listAllMenus(@Param("tenantId") Long tenantId);

        /**
     * Counts child menus.
     *
     * @param tenantId tenant id
     * @param parentId parent menu id
     * @return child count
     */
@Select("SELECT COUNT(1) FROM ai_menu WHERE tenant_id = #{tenantId} AND parent_id = #{parentId}")
    long countChildren(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId);
}
