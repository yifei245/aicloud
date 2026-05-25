package com.aicloud.module.mp.biz.service;

import com.aicloud.module.mp.biz.entity.AiMemberProfile;
import com.aicloud.module.mp.biz.entity.AiMpUserBind;
import com.aicloud.module.mp.biz.mapper.MemberProfileMapper;
import com.aicloud.module.mp.biz.mapper.MpUserBindMapper;
import com.aicloud.module.mp.biz.model.MpBindRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class MpUserService {

    private final MpUserBindMapper mpUserBindMapper;
    private final MemberProfileMapper memberProfileMapper;

    public MpUserService(MpUserBindMapper mpUserBindMapper, MemberProfileMapper memberProfileMapper) {
        this.mpUserBindMapper = mpUserBindMapper;
        this.memberProfileMapper = memberProfileMapper;
    }

    public Map<String, Object> profile(Long tenantId, Long userId, String username) {
        AiMemberProfile profile = memberProfileMapper.selectOne(new LambdaQueryWrapper<AiMemberProfile>()
                .eq(AiMemberProfile::getTenantId, tenantId)
                .eq(AiMemberProfile::getUserId, userId));
        AiMpUserBind bind = mpUserBindMapper.selectOne(new LambdaQueryWrapper<AiMpUserBind>()
                .eq(AiMpUserBind::getTenantId, tenantId)
                .eq(AiMpUserBind::getUserId, userId));
        Map<String, Object> data = new HashMap<>(8);
        data.put("tenantId", tenantId);
        data.put("userId", userId);
        data.put("username", username);
        data.put("scene", "mini-program");
        data.put("memberLevel", profile == null ? null : profile.getLevel());
        data.put("points", profile == null ? 0 : profile.getPoints());
        data.put("balance", profile == null ? 0 : profile.getBalance());
        data.put("bindInfo", bind);
        return data;
    }

    @Transactional(rollbackFor = Exception.class)
    public AiMpUserBind bind(Long tenantId, Long userId, MpBindRequest request) {
        if (!StringUtils.hasText(request.getOpenId())) {
            throw new IllegalArgumentException("openId 不能为空");
        }
        AiMpUserBind bind = mpUserBindMapper.selectOne(new LambdaQueryWrapper<AiMpUserBind>()
                .eq(AiMpUserBind::getTenantId, tenantId)
                .eq(AiMpUserBind::getUserId, userId));
        if (bind == null) {
            bind = new AiMpUserBind();
            bind.setTenantId(tenantId);
            bind.setUserId(userId);
            bind.setCreateTime(LocalDateTime.now());
            bind.setBindTime(LocalDateTime.now());
        }
        bind.setOpenId(request.getOpenId());
        bind.setUnionId(request.getUnionId());
        bind.setNickname(request.getNickname());
        bind.setAvatarUrl(request.getAvatarUrl());
        bind.setPhone(request.getPhone());
        bind.setStatus(1);
        bind.setUpdateTime(LocalDateTime.now());
        if (bind.getId() == null) {
            mpUserBindMapper.insert(bind);
        } else {
            mpUserBindMapper.updateById(bind);
        }
        if (StringUtils.hasText(request.getNickname()) || StringUtils.hasText(request.getPhone()) || StringUtils.hasText(request.getAvatarUrl())) {
            memberProfileMapper.update(null, new LambdaUpdateWrapper<AiMemberProfile>()
                    .eq(AiMemberProfile::getTenantId, tenantId)
                    .eq(AiMemberProfile::getUserId, userId)
                    .set(StringUtils.hasText(request.getNickname()), AiMemberProfile::getNickname, request.getNickname())
                    .set(StringUtils.hasText(request.getPhone()), AiMemberProfile::getMobile, request.getPhone())
                    .set(StringUtils.hasText(request.getAvatarUrl()), AiMemberProfile::getAvatar, request.getAvatarUrl()));
        }
        return bind;
    }

    public void unbind(Long tenantId, Long userId) {
        mpUserBindMapper.delete(new LambdaQueryWrapper<AiMpUserBind>()
                .eq(AiMpUserBind::getTenantId, tenantId)
                .eq(AiMpUserBind::getUserId, userId));
    }
}
