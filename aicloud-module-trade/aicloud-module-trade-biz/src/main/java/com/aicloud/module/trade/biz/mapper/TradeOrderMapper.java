package com.aicloud.module.trade.biz.mapper;

import com.aicloud.module.trade.biz.entity.AiTradeOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradeOrderMapper extends BaseMapper<AiTradeOrder> {
}
