package com.aicloud.module.system.biz.mapper;

import com.aicloud.module.system.biz.entity.AiAuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AiAuditLog> {
}
