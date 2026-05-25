package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiDept;
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
public interface DeptMapper extends BaseMapper<AiDept> {

        /**
     * Lists department rows.
     *
     * @param tenantId tenant id
     * @return department rows
     */
@Select("""
            SELECT d.id, d.parent_id, d.name, d.leader_user_id, d.sort, d.status,
                   u.nickname AS leader_nickname
            FROM ai_dept d
            LEFT JOIN ai_user u ON u.id = d.leader_user_id AND u.tenant_id = d.tenant_id
            WHERE d.tenant_id = #{tenantId}
            ORDER BY d.sort ASC, d.id ASC
            """)
    List<Map<String, Object>> listDeptRows(@Param("tenantId") Long tenantId);

        /**
     * Counts child departments.
     *
     * @param tenantId tenant id
     * @param deptId department id
     * @return child count
     */
@Select("SELECT COUNT(1) FROM ai_dept WHERE tenant_id = #{tenantId} AND parent_id = #{deptId}")
    long countChildren(@Param("tenantId") Long tenantId, @Param("deptId") Long deptId);

        /**
     * Counts users in department.
     *
     * @param tenantId tenant id
     * @param deptId department id
     * @return user count
     */
@Select("SELECT COUNT(1) FROM ai_user_dept_post WHERE tenant_id = #{tenantId} AND dept_id = #{deptId}")
    long countUsers(@Param("tenantId") Long tenantId, @Param("deptId") Long deptId);
}
