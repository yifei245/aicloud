package com.aicloud.auth.mapper;

import com.aicloud.auth.entity.AiLoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper extends BaseMapper<AiLoginLog> {
}
