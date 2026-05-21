package com.aicloud.module.openapi.biz.service;

import com.aicloud.module.openapi.biz.entity.AiMemberAccountLog;
import com.aicloud.module.openapi.biz.entity.AiMemberAddress;
import com.aicloud.module.openapi.biz.entity.AiMemberProfile;
import com.aicloud.module.openapi.biz.mapper.MemberAccountLogMapper;
import com.aicloud.module.openapi.biz.mapper.MemberAddressMapper;
import com.aicloud.module.openapi.biz.mapper.MemberProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OpenApiMemberService {

    private final MemberProfileMapper memberProfileMapper;
    private final MemberAddressMapper memberAddressMapper;
    private final MemberAccountLogMapper memberAccountLogMapper;
    private final OpenApiPlatformService platformService;

    public OpenApiMemberService(MemberProfileMapper memberProfileMapper, MemberAddressMapper memberAddressMapper,
                                MemberAccountLogMapper memberAccountLogMapper, OpenApiPlatformService platformService) {
        this.memberProfileMapper = memberProfileMapper;
        this.memberAddressMapper = memberAddressMapper;
        this.memberAccountLogMapper = memberAccountLogMapper;
        this.platformService = platformService;
    }

    public Map<String, Object> summary(Long tenantId, Long memberId, String appKey) {
        long start = System.currentTimeMillis();
        AiMemberProfile profile = findProfile(tenantId, memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("memberId", memberId);
        data.put("tenantId", tenantId);
        data.put("appKey", appKey);
        data.put("level", profile == null ? null : profile.getLevel());
        data.put("status", profile == null ? null : (profile.getStatus() != null && profile.getStatus() == 1 ? "ACTIVE" : "DISABLED"));
        data.put("points", profile == null ? 0 : profile.getPoints());
        data.put("balance", profile == null ? 0 : profile.getBalance());
        platformService.recordCall(tenantId, appKey, "/openapi/v1/member/summary", "GET", true,
                (int) (System.currentTimeMillis() - start), null);
        return data;
    }

    public Map<String, Object> detail(Long tenantId, Long memberId, String appKey) {
        long start = System.currentTimeMillis();
        AiMemberProfile profile = findProfile(tenantId, memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("tenantId", tenantId);
        data.put("appKey", appKey);
        data.put("member", profile);
        data.put("addresses", listAddresses(tenantId, memberId));
        platformService.recordCall(tenantId, appKey, "/openapi/v1/member/detail", "GET", true,
                (int) (System.currentTimeMillis() - start), null);
        return data;
    }

    public List<AiMemberAccountLog> accountLogs(Long tenantId, Long memberId) {
        return memberAccountLogMapper.selectList(new LambdaQueryWrapper<AiMemberAccountLog>()
                .eq(AiMemberAccountLog::getTenantId, tenantId)
                .eq(AiMemberAccountLog::getUserId, memberId)
                .orderByDesc(AiMemberAccountLog::getId));
    }

    private AiMemberProfile findProfile(Long tenantId, Long memberId) {
        return memberProfileMapper.selectOne(new LambdaQueryWrapper<AiMemberProfile>()
                .eq(AiMemberProfile::getTenantId, tenantId)
                .eq(AiMemberProfile::getUserId, memberId)
                .last("LIMIT 1"));
    }

    private List<AiMemberAddress> listAddresses(Long tenantId, Long memberId) {
        return memberAddressMapper.selectList(new LambdaQueryWrapper<AiMemberAddress>()
                .eq(AiMemberAddress::getTenantId, tenantId)
                .eq(AiMemberAddress::getUserId, memberId)
                .eq(AiMemberAddress::getStatus, 1)
                .orderByDesc(AiMemberAddress::getDefaultStatus)
                .orderByDesc(AiMemberAddress::getId));
    }
}
