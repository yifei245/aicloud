package com.aicloud.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class GatewayAuditLogService {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayAuditLogService.class);

    @Async("auditTaskExecutor")
    public void logAsync(String result, String reason, String method, String path,
                         String tenantId, String userId, String username,
                         String requestIp, String effect, String permission, Integer priority) {
        // Gateway must stay non-blocking and DB-free; durable audit persistence belongs to an audit/infra service.
        LOG.info("gateway_audit result={} reason={} method={} path={} tenantId={} userId={} username={} ip={} effect={} permission={} priority={}",
                result, reason, method, path, tenantId, userId, username, requestIp, effect, permission, priority);
    }
}
