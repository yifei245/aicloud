package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiDictType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DictTypeMapper extends BaseMapper<AiDictType> {
    @Select("SELECT COUNT(1) FROM ai_dict_type WHERE tenant_id = #{tenantId} AND dict_type = #{dictType} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    long countByType(@Param("tenantId") Long tenantId, @Param("dictType") String dictType, @Param("excludeId") Long excludeId);
}
