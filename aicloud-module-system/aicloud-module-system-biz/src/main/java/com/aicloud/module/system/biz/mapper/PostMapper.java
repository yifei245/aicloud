package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostMapper extends BaseMapper<AiPost> {
    @Select("SELECT COUNT(1) FROM ai_post WHERE tenant_id = #{tenantId} AND code = #{code} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    long countByCode(@Param("tenantId") Long tenantId, @Param("code") String code, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(1) FROM ai_user_dept_post WHERE tenant_id = #{tenantId} AND post_id = #{postId}")
    long countUsers(@Param("tenantId") Long tenantId, @Param("postId") Long postId);
}
