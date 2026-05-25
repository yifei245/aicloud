package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiAuditLog;
import com.aicloud.module.system.biz.entity.AiLoginLog;
import com.aicloud.module.system.biz.mapper.AuditLogMapper;
import com.aicloud.module.system.biz.mapper.LoginLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class LogAdminService {

    private final AuditLogMapper auditLogMapper;
    private final LoginLogMapper loginLogMapper;

    public LogAdminService(AuditLogMapper auditLogMapper, LoginLogMapper loginLogMapper) {
        this.auditLogMapper = auditLogMapper;
        this.loginLogMapper = loginLogMapper;
    }

    public List<AiAuditLog> listAuditLogs(Long tenantId, String module, String username, Integer success) {
        return auditLogMapper.selectList(new LambdaQueryWrapper<AiAuditLog>()
                .eq(AiAuditLog::getTenantId, tenantId)
                .like(StringUtils.hasText(module), AiAuditLog::getModule, module)
                .like(StringUtils.hasText(username), AiAuditLog::getUsername, username)
                .eq(success != null, AiAuditLog::getSuccess, success)
                .orderByDesc(AiAuditLog::getId));
    }

    public List<AiLoginLog> listLoginLogs(Long tenantId, String username, String terminal, Integer success) {
        return loginLogMapper.selectList(new LambdaQueryWrapper<AiLoginLog>()
                .eq(AiLoginLog::getTenantId, tenantId)
                .like(StringUtils.hasText(username), AiLoginLog::getUsername, username)
                .eq(StringUtils.hasText(terminal), AiLoginLog::getLoginTerminal, terminal)
                .eq(success != null, AiLoginLog::getSuccess, success)
                .orderByDesc(AiLoginLog::getId));
    }
}
