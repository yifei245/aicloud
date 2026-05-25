package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Mapper
public interface UserMapper extends BaseMapper<AiUser> {

        /**
     * Lists user rows for management page.
     *
     * @param tenantId tenant id
     * @param keyword search keyword
     * @param status user status
     * @param deptId department id
     * @return user rows
     */
@Select("""
            SELECT u.id, u.tenant_id, u.username, u.nickname, u.mobile, u.email, u.user_type, u.status,
                   u.last_login_time, u.create_time,
                   udp.dept_id, d.name AS dept_name
            FROM ai_user u
            LEFT JOIN ai_user_dept_post udp ON udp.user_id = u.id AND udp.tenant_id = u.tenant_id
            LEFT JOIN ai_dept d ON d.id = udp.dept_id AND d.tenant_id = u.tenant_id
            WHERE u.tenant_id = #{tenantId}
              AND (#{keyword} IS NULL OR #{keyword} = '' OR u.username LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%') OR u.mobile LIKE CONCAT('%', #{keyword}, '%') OR u.email LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{status} IS NULL OR u.status = #{status})
              AND (#{deptId} IS NULL OR udp.dept_id = #{deptId})
            GROUP BY u.id, u.tenant_id, u.username, u.nickname, u.mobile, u.email, u.user_type, u.status,
                     u.last_login_time, u.create_time, udp.dept_id, d.name
            ORDER BY u.id DESC
            """)
    List<Map<String, Object>> listUserRows(@Param("tenantId") Long tenantId,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status,
                                           @Param("deptId") Long deptId);

        /**
     * Lists user role ids.
     *
     * @param tenantId tenant id
     * @param userId user id
     * @return role ids
     */
@Select("SELECT role_id FROM ai_user_role WHERE tenant_id = #{tenantId} AND user_id = #{userId} ORDER BY role_id")
    List<Long> listRoleIds(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

        /**
     * Lists user post ids.
     *
     * @param tenantId tenant id
     * @param userId user id
     * @return post ids
     */
@Select("SELECT post_id FROM ai_user_dept_post WHERE tenant_id = #{tenantId} AND user_id = #{userId} AND post_id IS NOT NULL ORDER BY post_id")
    List<Long> listPostIds(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

        /**
     * Lists user post names.
     *
     * @param tenantId tenant id
     * @param userId user id
     * @return post names
     */
@Select("SELECT p.name FROM ai_post p INNER JOIN ai_user_dept_post udp ON udp.post_id = p.id AND udp.tenant_id = p.tenant_id WHERE udp.tenant_id = #{tenantId} AND udp.user_id = #{userId} ORDER BY p.sort ASC, p.id ASC")
    List<String> listPostNames(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

        /**
     * Lists user role codes.
     *
     * @param tenantId tenant id
     * @param userId user id
     * @return role codes
     */
@Select("SELECT r.code FROM ai_role r INNER JOIN ai_user_role ur ON ur.role_id = r.id AND ur.tenant_id = r.tenant_id WHERE ur.tenant_id = #{tenantId} AND ur.user_id = #{userId} ORDER BY r.sort ASC, r.id ASC")
    List<String> listRoleCodes(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

        /**
     * Finds primary department id.
     *
     * @param tenantId tenant id
     * @param userId user id
     * @return department id
     */
@Select("SELECT dept_id FROM ai_user_dept_post WHERE tenant_id = #{tenantId} AND user_id = #{userId} ORDER BY id ASC LIMIT 1")
    Long findPrimaryDeptId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

        /**
     * Counts users by username.
     *
     * @param tenantId tenant id
     * @param username username
     * @param excludeId excluded id
     * @return matched count
     */
@Select("SELECT COUNT(1) FROM ai_user WHERE tenant_id = #{tenantId} AND username = #{username} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    long countByUsername(@Param("tenantId") Long tenantId, @Param("username") String username, @Param("excludeId") Long excludeId);
}
