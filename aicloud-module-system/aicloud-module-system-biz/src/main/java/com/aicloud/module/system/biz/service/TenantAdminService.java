package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiTenant;
import com.aicloud.module.system.biz.mapper.TenantMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class TenantAdminService {

    private final TenantMapper tenantMapper;

    public TenantAdminService(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    public List<AiTenant> listEnabled() {
        return tenantMapper.selectList(new LambdaQueryWrapper<AiTenant>()
                .eq(AiTenant::getStatus, 1)
                .orderByAsc(AiTenant::getId));
    }
}
