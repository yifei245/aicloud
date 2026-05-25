package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiDictType;
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
public interface DictTypeMapper extends BaseMapper<AiDictType> {

    /**
     * Counts dictionary types by type.
     *
     * @param tenantId tenant id
     * @param dictType dictionary type
     * @param excludeId excluded id
     * @return matched count
     */
    @Select("SELECT COUNT(1) FROM ai_dict_type WHERE tenant_id = #{tenantId} AND dict_type = #{dictType} AND (#{excludeId} IS NULL OR id != #{excludeId})")
    long countByType(@Param("tenantId") Long tenantId, @Param("dictType") String dictType, @Param("excludeId") Long excludeId);
}
