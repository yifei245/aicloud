package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Mapper
public interface PostMapper extends BaseMapper<AiPost> {

    /**
     * Counts posts by code.
     *
     * @param tenantId tenant id
     * @param code post code
     * @param excludeId excluded id
     * @return matched count
     */
    @Select("SELECT COUNT(1) FROM ai_post WHERE tenant_id = #{tenantId} AND code = #{code} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    long countByCode(@Param("tenantId") Long tenantId, @Param("code") String code, @Param("excludeId") Long excludeId);

    /**
     * Counts users bound to a post.
     *
     * @param tenantId tenant id
     * @param postId post id
     * @return user count
     */
    @Select("SELECT COUNT(1) FROM ai_user_dept_post WHERE tenant_id = #{tenantId} AND post_id = #{postId}")
    long countUsers(@Param("tenantId") Long tenantId, @Param("postId") Long postId);
}
