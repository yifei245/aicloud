package com.aicloud.module.openapi.biz.service;

import com.aicloud.module.openapi.biz.entity.AiOpenApiApp;
import com.aicloud.module.openapi.biz.entity.AiOpenApiCallLog;
import com.aicloud.module.openapi.biz.entity.AiOpenApiWebhook;
import com.aicloud.module.openapi.biz.mapper.OpenApiAppMapper;
import com.aicloud.module.openapi.biz.mapper.OpenApiCallLogMapper;
import com.aicloud.module.openapi.biz.mapper.OpenApiWebhookMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OpenApiPlatformService {

    private final OpenApiAppMapper appMapper;
    private final OpenApiCallLogMapper callLogMapper;
    private final OpenApiWebhookMapper webhookMapper;

    public OpenApiPlatformService(OpenApiAppMapper appMapper, OpenApiCallLogMapper callLogMapper,
                                  OpenApiWebhookMapper webhookMapper) {
        this.appMapper = appMapper;
        this.callLogMapper = callLogMapper;
        this.webhookMapper = webhookMapper;
    }

    public List<AiOpenApiApp> listApps(Long tenantId, String keyword) {
        return appMapper.selectList(new LambdaQueryWrapper<AiOpenApiApp>()
                .eq(AiOpenApiApp::getTenantId, tenantId)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(AiOpenApiApp::getAppName, keyword)
                        .or().like(AiOpenApiApp::getAppKey, keyword))
                .orderByDesc(AiOpenApiApp::getId));
    }

    @Transactional
    public AiOpenApiApp saveApp(Long tenantId, AiOpenApiApp app) {
        LocalDateTime now = LocalDateTime.now();
        app.setTenantId(tenantId);
        if (!StringUtils.hasText(app.getAppKey())) {
            app.setAppKey("ak_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        }
        if (!StringUtils.hasText(app.getAppSecret())) {
            app.setAppSecret("sk_" + UUID.randomUUID().toString().replace("-", ""));
        }
        if (app.getStatus() == null) {
            app.setStatus(1);
        }
        if (app.getQpsLimit() == null) {
            app.setQpsLimit(50);
        }
        if (app.getDailyLimit() == null) {
            app.setDailyLimit(10000);
        }
        app.setUpdateTime(now);
        if (app.getId() == null) {
            app.setCreateTime(now);
            appMapper.insert(app);
        } else {
            appMapper.updateById(app);
        }
        return app;
    }

    public void updateAppStatus(Long tenantId, Long id, Integer status) {
        AiOpenApiApp app = appMapper.selectById(id);
        if (app == null || !tenantId.equals(app.getTenantId())) {
            throw new IllegalArgumentException("开放应用不存在");
        }
        app.setStatus(status);
        app.setUpdateTime(LocalDateTime.now());
        appMapper.updateById(app);
    }

    public List<AiOpenApiCallLog> listLogs(Long tenantId, String appKey, String apiPath) {
        return callLogMapper.selectList(new LambdaQueryWrapper<AiOpenApiCallLog>()
                .eq(AiOpenApiCallLog::getTenantId, tenantId)
                .eq(StringUtils.hasText(appKey), AiOpenApiCallLog::getAppKey, appKey)
                .like(StringUtils.hasText(apiPath), AiOpenApiCallLog::getApiPath, apiPath)
                .orderByDesc(AiOpenApiCallLog::getId)
                .last("LIMIT 100"));
    }

    public void recordCall(Long tenantId, String appKey, String apiPath, String method, boolean success, Integer costMs, String errorMsg) {
        AiOpenApiCallLog log = new AiOpenApiCallLog();
        log.setTenantId(tenantId);
        log.setAppKey(StringUtils.hasText(appKey) ? appKey : "anonymous");
        log.setApiPath(apiPath);
        log.setMethod(method);
        log.setRequestId(UUID.randomUUID().toString());
        log.setRequestIp("gateway");
        log.setSuccess(success ? 1 : 0);
        log.setCostMs(costMs == null ? 0 : costMs);
        log.setErrorMsg(errorMsg);
        log.setCreateTime(LocalDateTime.now());
        callLogMapper.insert(log);
    }

    public List<AiOpenApiWebhook> listWebhooks(Long tenantId, String appKey) {
        return webhookMapper.selectList(new LambdaQueryWrapper<AiOpenApiWebhook>()
                .eq(AiOpenApiWebhook::getTenantId, tenantId)
                .eq(StringUtils.hasText(appKey), AiOpenApiWebhook::getAppKey, appKey)
                .orderByDesc(AiOpenApiWebhook::getId));
    }

    @Transactional
    public AiOpenApiWebhook saveWebhook(Long tenantId, AiOpenApiWebhook webhook) {
        LocalDateTime now = LocalDateTime.now();
        webhook.setTenantId(tenantId);
        if (webhook.getStatus() == null) {
            webhook.setStatus(1);
        }
        webhook.setUpdateTime(now);
        if (webhook.getId() == null) {
            webhook.setCreateTime(now);
            webhookMapper.insert(webhook);
        } else {
            webhookMapper.updateById(webhook);
        }
        return webhook;
    }

    public Map<String, Object> testWebhook(Long tenantId, Long id) {
        AiOpenApiWebhook webhook = webhookMapper.selectById(id);
        if (webhook == null || !tenantId.equals(webhook.getTenantId())) {
            throw new IllegalArgumentException("Webhook 不存在");
        }
        webhook.setLastPushTime(LocalDateTime.now());
        webhook.setUpdateTime(LocalDateTime.now());
        webhookMapper.updateById(webhook);
        Map<String, Object> data = new HashMap<>();
        data.put("webhookId", webhook.getId());
        data.put("eventType", webhook.getEventType());
        data.put("targetUrl", webhook.getTargetUrl());
        data.put("signature", "mock-signature-" + webhook.getId());
        data.put("pushed", true);
        data.put("pushTime", webhook.getLastPushTime());
        return data;
    }
}
