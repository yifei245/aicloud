package com.aicloud.framework.web.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default audit publisher writes structured log only.
 *
 * @author yifei
 */
public class DefaultAuditLogPublisher implements AuditLogPublisher {

    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AicloudOperationAudit");

    @Override
    public void publish(AuditLogEvent event) {
        AUDIT_LOG.info("operation_audit tenantId={} userId={} username={} module={} operation={} method={} uri={} ip={} success={} costMs={} error={}",
                event.getTenantId(),
                event.getUserId(),
                event.getUsername(),
                event.getModule(),
                event.getOperation(),
                event.getRequestMethod(),
                event.getRequestUri(),
                event.getRequestIp(),
                event.getSuccess(),
                event.getCostMillis(),
                event.getErrorMsg());
    }
}
