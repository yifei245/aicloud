package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiSystemConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SystemConfigMapper extends BaseMapper<AiSystemConfig> {
    @Select("SELECT COUNT(1) FROM ai_system_config WHERE tenant_id = #{tenantId} AND config_key = #{configKey} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    long countByKey(@Param("tenantId") Long tenantId, @Param("configKey") String configKey, @Param("excludeId") Long excludeId);
}
