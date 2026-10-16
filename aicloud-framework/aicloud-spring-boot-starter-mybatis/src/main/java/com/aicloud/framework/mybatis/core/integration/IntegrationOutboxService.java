package com.aicloud.framework.mybatis.core.integration;

import com.aicloud.framework.common.exception.ServiceException;
import com.aicloud.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Transactional source records with lease-based, retryable delivery. */
@Slf4j
public class IntegrationOutboxService {
    private final JdbcTemplate jdbc;
    private final String producer;
    private final Supplier<List<IntegrationEventHandler>> handlerProvider;

    public IntegrationOutboxService(JdbcTemplate jdbc, String producer, Supplier<List<IntegrationEventHandler>> handlers) {
        this.jdbc = jdbc;
        this.producer = producer;
        this.handlerProvider = handlers;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long enqueue(Long tenantId, String eventType, String businessKey, Object payload) {
        return enqueueOrdered(tenantId, eventType, businessKey, businessKey, payload);
    }

    /** Preserve causal order for transitions of the same business document. */
    @Transactional(rollbackFor = Exception.class)
    public Long enqueueOrdered(Long tenantId, String eventType, String businessKey, String aggregateKey, Object payload) {
        if (tenantId == null || tenantId <= 0 || businessKey == null || businessKey.isBlank() || businessKey.length() > 200 || !handlers().containsKey(eventType)) {
            throw new IllegalArgumentException("Invalid tenant, business key or integration event type");
        }
        if (aggregateKey == null || aggregateKey.isBlank() || aggregateKey.length() > 200) throw new IllegalArgumentException("Invalid aggregate key");
        String body = JsonUtils.toJsonString(payload);
        try {
            jdbc.update("INSERT INTO integration_outbox(tenant_id,producer,event_type,business_key,aggregate_key,payload,status,next_attempt_time) VALUES(?,?,?,?,?,?,'PENDING',CURRENT_TIMESTAMP)",
                    tenantId, producer, eventType, businessKey, aggregateKey, body);
        } catch (DuplicateKeyException duplicate) {
            var previous = jdbc.queryForMap("SELECT payload,event_type,aggregate_key FROM integration_outbox WHERE tenant_id=? AND producer=? AND business_key=?",
                    tenantId, producer, businessKey);
            if (!body.equals(previous.get("payload")) || !eventType.equals(previous.get("event_type"))
                    || !aggregateKey.equals(previous.get("aggregate_key"))) {
                throw new IllegalArgumentException("Integration business key already has different content or ordering");
            }
        }
        return jdbc.queryForObject("SELECT id FROM integration_outbox WHERE tenant_id=? AND producer=? AND business_key=?", Long.class,
                tenantId, producer, businessKey);
    }

    /** Used only by after-completion hooks and rollback compensation. */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Long enqueueIndependent(Long tenantId, String eventType, String businessKey, Object payload) {
        return enqueue(tenantId, eventType, businessKey, payload);
    }

    @Scheduled(initialDelayString = "${aicloud.integration.retry-delay-ms:2000}", fixedDelayString = "${aicloud.integration.retry-delay-ms:2000}")
    public void dispatch() {
        Map<String, IntegrationEventHandler> handlers = handlers();
        if (handlers.isEmpty()) return;
        List<IntegrationEvent> pending = jdbc.query("SELECT id,tenant_id,producer,event_type,business_key,payload,attempts FROM integration_outbox o "
                        + "WHERE producer=? AND ((status IN ('PENDING','RETRY') AND next_attempt_time<=CURRENT_TIMESTAMP) OR (status='PROCESSING' AND lease_until<CURRENT_TIMESTAMP)) "
                        + "AND NOT EXISTS (SELECT 1 FROM integration_outbox prior WHERE prior.tenant_id=o.tenant_id AND prior.producer=o.producer AND prior.aggregate_key=o.aggregate_key AND prior.id<o.id AND prior.status<>'DONE') ORDER BY id LIMIT 50",
                (rs, n) -> new IntegrationEvent().setId(rs.getLong("id")).setTenantId(rs.getLong("tenant_id"))
                        .setProducer(rs.getString("producer")).setEventType(rs.getString("event_type"))
                        .setBusinessKey(rs.getString("business_key")).setPayload(rs.getString("payload")).setAttempts(rs.getInt("attempts")), producer);
        for (IntegrationEvent event : pending) {
            String token = UUID.randomUUID().toString();
            int claimed = jdbc.update("UPDATE integration_outbox SET status='PROCESSING',lock_token=?,lease_until=?,attempts=attempts+1 "
                            + "WHERE id=? AND ((status IN ('PENDING','RETRY') AND next_attempt_time<=CURRENT_TIMESTAMP) OR (status='PROCESSING' AND lease_until<CURRENT_TIMESTAMP))",
                    token, LocalDateTime.now().plusMinutes(2), event.getId());
            if (claimed == 0) continue;
            try {
                IntegrationEventHandler handler = handlers.get(event.getEventType());
                if (handler == null) throw new IllegalStateException("Integration handler is unavailable");
                handler.handle(event);
                jdbc.update("UPDATE integration_outbox SET status='DONE',last_error=NULL,lock_token=NULL,lease_until=NULL,update_time=CURRENT_TIMESTAMP WHERE id=? AND lock_token=?",
                        event.getId(), token);
            } catch (Exception error) {
                String reason = error.getClass().getSimpleName() + (error instanceof ServiceException service ? ":" + service.getCode() + ":" + service.getMessage() : "");
                reason=reason.replaceAll("[\\r\\n]"," ").replaceAll("(?i)(bearer|access_token|password|secret)[=: ]+[^ ,;]+","$1=[redacted]");
                if(reason.length()>200) reason=reason.substring(0,200);
                long delay = Math.min(300, 2L << Math.min(event.getAttempts(), 7));
                jdbc.update("UPDATE integration_outbox SET status='RETRY',last_error=?,next_attempt_time=?,lock_token=NULL,lease_until=NULL,update_time=CURRENT_TIMESTAMP WHERE id=? AND lock_token=?",
                        reason, LocalDateTime.now().plusSeconds(delay), event.getId(), token);
                log.warn("Integration delivery pending: producer={}, eventId={}, type={}, failure={}", producer, event.getId(), event.getEventType(), reason);
            }
        }
    }

    private Map<String, IntegrationEventHandler> handlers() {
        return handlerProvider.get().stream().collect(Collectors.toMap(IntegrationEventHandler::eventType, Function.identity()));
    }
}
