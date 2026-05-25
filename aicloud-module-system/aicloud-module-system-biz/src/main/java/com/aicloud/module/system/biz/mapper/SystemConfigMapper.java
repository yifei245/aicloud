package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiSystemConfig;
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
public interface SystemConfigMapper extends BaseMapper<AiSystemConfig> {

    /**
     * Counts configs by key.
     *
     * @param tenantId tenant id
     * @param configKey config key
     * @param excludeId excluded id
     * @return matched count
     */
    @Select("SELECT COUNT(1) FROM ai_system_config WHERE tenant_id = #{tenantId} AND config_key = #{configKey} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    long countByKey(@Param("tenantId") Long tenantId, @Param("configKey") String configKey, @Param("excludeId") Long excludeId);
}
