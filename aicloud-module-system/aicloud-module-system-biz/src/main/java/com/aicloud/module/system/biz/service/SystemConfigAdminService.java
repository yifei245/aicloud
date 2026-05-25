package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiSystemConfig;
import com.aicloud.module.system.biz.mapper.SystemConfigMapper;
import com.aicloud.module.system.biz.model.config.SystemConfigSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class SystemConfigAdminService {

    private final SystemConfigMapper systemConfigMapper;

    public SystemConfigAdminService(SystemConfigMapper systemConfigMapper) {
        this.systemConfigMapper = systemConfigMapper;
    }

    public List<AiSystemConfig> list(Long tenantId, String keyword, Integer status) {
        return systemConfigMapper.selectList(new LambdaQueryWrapper<AiSystemConfig>()
                .eq(AiSystemConfig::getTenantId, tenantId)
                .and(StringUtils.hasText(keyword), w -> w.like(AiSystemConfig::getConfigKey, keyword).or().like(AiSystemConfig::getConfigName, keyword))
                .eq(status != null, AiSystemConfig::getStatus, status)
                .orderByAsc(AiSystemConfig::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiSystemConfig save(Long tenantId, SystemConfigSaveRequest request) {
        if (!StringUtils.hasText(request.getConfigKey()) || !StringUtils.hasText(request.getConfigName())) {
            throw new IllegalArgumentException("参数键和值名称不能为空");
        }
        if (systemConfigMapper.countByKey(tenantId, request.getConfigKey(), request.getId()) > 0) {
            throw new IllegalArgumentException("参数键已存在");
        }
        AiSystemConfig entity = request.getId() == null ? new AiSystemConfig() : systemConfigMapper.selectById(request.getId());
        if (entity == null) {
            throw new IllegalArgumentException("参数不存在");
        }
        if (request.getId() == null) {
            entity.setTenantId(tenantId);
            entity.setCreateTime(LocalDateTime.now());
        }
        entity.setConfigKey(request.getConfigKey());
        entity.setConfigName(request.getConfigName());
        entity.setConfigValue(request.getConfigValue());
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setRemark(request.getRemark());
        entity.setUpdateTime(LocalDateTime.now());
        if (request.getId() == null) {
            systemConfigMapper.insert(entity);
        } else {
            systemConfigMapper.updateById(entity);
        }
        return entity;
    }

    public void delete(Long tenantId, Long id) {
        systemConfigMapper.delete(new LambdaQueryWrapper<AiSystemConfig>()
                .eq(AiSystemConfig::getTenantId, tenantId)
                .eq(AiSystemConfig::getId, id));
    }
}
