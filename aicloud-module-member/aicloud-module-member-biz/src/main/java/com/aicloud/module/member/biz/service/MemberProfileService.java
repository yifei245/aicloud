package com.aicloud.module.member.biz.service;

import com.aicloud.module.member.biz.entity.AiMemberAddress;
import com.aicloud.module.member.biz.entity.AiMemberLevel;
import com.aicloud.module.member.biz.entity.AiMemberProfile;
import com.aicloud.module.member.biz.mapper.MemberAddressMapper;
import com.aicloud.module.member.biz.mapper.MemberLevelMapper;
import com.aicloud.module.member.biz.mapper.MemberProfileMapper;
import com.aicloud.module.member.biz.model.address.MemberAddressSaveRequest;
import com.aicloud.module.member.biz.model.common.TerminalUserContext;
import com.aicloud.module.member.biz.model.profile.MemberProfileResponse;
import com.aicloud.module.member.biz.model.profile.MemberProfileUpdateRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MemberProfileService {

    private final MemberProfileMapper memberProfileMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final MemberAddressMapper memberAddressMapper;

    public MemberProfileService(MemberProfileMapper memberProfileMapper, MemberLevelMapper memberLevelMapper,
                                MemberAddressMapper memberAddressMapper) {
        this.memberProfileMapper = memberProfileMapper;
        this.memberLevelMapper = memberLevelMapper;
        this.memberAddressMapper = memberAddressMapper;
    }

    public MemberProfileResponse getProfile(TerminalUserContext context) {
        AiMemberProfile profile = getOrCreateProfile(context);
        AiMemberLevel level = findLevel(context.getTenantId(), profile.getLevel());
        MemberProfileResponse response = new MemberProfileResponse();
        response.setTenantId(context.getTenantId());
        response.setUserId(context.getUserId());
        response.setUsername(context.getUsername());
        response.setUserType(context.getUserType());
        response.setTerminal(context.getTerminal());
        response.setNickname(profile.getNickname());
        response.setMobile(profile.getMobile());
        response.setEmail(profile.getEmail());
        response.setAvatar(profile.getAvatar());
        response.setGender(profile.getGender());
        response.setBirthday(profile.getBirthday() == null ? null : profile.getBirthday().toString());
        response.setLevelCode(profile.getLevel());
        response.setLevelName(level == null ? profile.getLevel() : level.getLevelName());
        response.setPoints(profile.getPoints());
        response.setBalance(profile.getBalance());
        response.setStatus(profile.getStatus());
        return response;
    }

    @Transactional
    public MemberProfileResponse updateProfile(TerminalUserContext context, MemberProfileUpdateRequest request) {
        AiMemberProfile profile = getOrCreateProfile(context);
        if (StringUtils.hasText(request.getNickname())) {
            profile.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getMobile())) {
            profile.setMobile(request.getMobile());
        }
        if (StringUtils.hasText(request.getEmail())) {
            profile.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            profile.setAvatar(request.getAvatar());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (StringUtils.hasText(request.getBirthday())) {
            profile.setBirthday(LocalDate.parse(request.getBirthday()));
        }
        profile.setUpdateTime(LocalDateTime.now());
        memberProfileMapper.updateById(profile);
        return getProfile(context);
    }

    public List<AiMemberLevel> listLevels(Long tenantId) {
        return memberLevelMapper.selectList(new LambdaQueryWrapper<AiMemberLevel>()
                .eq(AiMemberLevel::getTenantId, tenantId)
                .eq(AiMemberLevel::getStatus, 1)
                .orderByAsc(AiMemberLevel::getThresholdPoints, AiMemberLevel::getId));
    }

    public List<AiMemberAddress> listAddresses(TerminalUserContext context) {
        return memberAddressMapper.selectList(new LambdaQueryWrapper<AiMemberAddress>()
                .eq(AiMemberAddress::getTenantId, context.getTenantId())
                .eq(AiMemberAddress::getUserId, context.getUserId())
                .eq(AiMemberAddress::getStatus, 1)
                .orderByDesc(AiMemberAddress::getDefaultStatus)
                .orderByDesc(AiMemberAddress::getId));
    }

    @Transactional
    public AiMemberAddress saveAddress(TerminalUserContext context, MemberAddressSaveRequest request) {
        validateAddress(request);
        AiMemberAddress address = request.getId() == null ? new AiMemberAddress() : memberAddressMapper.selectOne(
                new LambdaQueryWrapper<AiMemberAddress>()
                        .eq(AiMemberAddress::getTenantId, context.getTenantId())
                        .eq(AiMemberAddress::getUserId, context.getUserId())
                        .eq(AiMemberAddress::getId, request.getId()));
        if (address == null) {
            address = new AiMemberAddress();
            address.setTenantId(context.getTenantId());
            address.setUserId(context.getUserId());
            address.setCreateTime(LocalDateTime.now());
            address.setStatus(1);
        }
        if (request.getDefaultStatus() != null && request.getDefaultStatus() == 1) {
            memberAddressMapper.update(null, new LambdaUpdateWrapper<AiMemberAddress>()
                    .eq(AiMemberAddress::getTenantId, context.getTenantId())
                    .eq(AiMemberAddress::getUserId, context.getUserId())
                    .set(AiMemberAddress::getDefaultStatus, 0));
        }
        address.setReceiverName(request.getReceiverName());
        address.setMobile(request.getMobile());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setDefaultStatus(request.getDefaultStatus() != null && request.getDefaultStatus() == 1 ? 1 : 0);
        address.setUpdateTime(LocalDateTime.now());
        if (address.getId() == null) {
            memberAddressMapper.insert(address);
        } else {
            memberAddressMapper.updateById(address);
        }
        return address;
    }

    public void deleteAddress(TerminalUserContext context, Long id) {
        memberAddressMapper.delete(new LambdaQueryWrapper<AiMemberAddress>()
                .eq(AiMemberAddress::getTenantId, context.getTenantId())
                .eq(AiMemberAddress::getUserId, context.getUserId())
                .eq(AiMemberAddress::getId, id));
    }

    private void validateAddress(MemberAddressSaveRequest request) {
        if (!StringUtils.hasText(request.getReceiverName()) || !StringUtils.hasText(request.getMobile())
                || !StringUtils.hasText(request.getProvince()) || !StringUtils.hasText(request.getCity())
                || !StringUtils.hasText(request.getDistrict()) || !StringUtils.hasText(request.getDetailAddress())) {
            throw new IllegalArgumentException("地址信息不完整");
        }
    }

    private AiMemberProfile getOrCreateProfile(TerminalUserContext context) {
        AiMemberProfile profile = memberProfileMapper.selectOne(new LambdaQueryWrapper<AiMemberProfile>()
                .eq(AiMemberProfile::getTenantId, context.getTenantId())
                .eq(AiMemberProfile::getUserId, context.getUserId()));
        if (profile != null) {
            return profile;
        }
        profile = new AiMemberProfile();
        profile.setTenantId(context.getTenantId());
        profile.setUserId(context.getUserId());
        profile.setNickname(context.getUsername());
        profile.setLevel("NORMAL");
        profile.setPoints(0L);
        profile.setBalance(java.math.BigDecimal.ZERO);
        profile.setGender(0);
        profile.setStatus(1);
        profile.setCreateTime(LocalDateTime.now());
        profile.setUpdateTime(LocalDateTime.now());
        memberProfileMapper.insert(profile);
        return profile;
    }

    private AiMemberLevel findLevel(Long tenantId, String levelCode) {
        return memberLevelMapper.selectOne(new LambdaQueryWrapper<AiMemberLevel>()
                .eq(AiMemberLevel::getTenantId, tenantId)
                .eq(AiMemberLevel::getLevelCode, levelCode)
                .last("LIMIT 1"));
    }
}
