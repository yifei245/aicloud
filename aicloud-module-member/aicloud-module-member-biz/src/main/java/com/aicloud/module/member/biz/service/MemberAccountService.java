package com.aicloud.module.member.biz.service;

import com.aicloud.module.member.biz.entity.AiMemberAccountLog;
import com.aicloud.module.member.biz.mapper.MemberAccountLogMapper;
import com.aicloud.module.member.biz.model.common.TerminalUserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MemberAccountService {

    private final MemberAccountLogMapper memberAccountLogMapper;

    public MemberAccountService(MemberAccountLogMapper memberAccountLogMapper) {
        this.memberAccountLogMapper = memberAccountLogMapper;
    }

    public List<AiMemberAccountLog> listLogs(TerminalUserContext context, String bizType) {
        return memberAccountLogMapper.selectList(new LambdaQueryWrapper<AiMemberAccountLog>()
                .eq(AiMemberAccountLog::getTenantId, context.getTenantId())
                .eq(AiMemberAccountLog::getUserId, context.getUserId())
                .eq(StringUtils.hasText(bizType), AiMemberAccountLog::getBizType, bizType)
                .orderByDesc(AiMemberAccountLog::getId));
    }
}
