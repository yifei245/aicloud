package com.aicloud.module.pay.biz.mapper;

import com.aicloud.module.pay.biz.entity.AiPayOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayOrderMapper extends BaseMapper<AiPayOrder> {
}
