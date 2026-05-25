package com.aicloud.module.member.biz.service;

import com.aicloud.module.member.biz.entity.*;
import com.aicloud.module.member.biz.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class MemberExtensionService {

    private static final String STATUS_FIELD = "status";
    private static final String SET_TENANT_ID_METHOD = "setTenantId";
    private static final String SET_CREATE_TIME_METHOD = "setCreateTime";
    private static final String SET_UPDATE_TIME_METHOD = "setUpdateTime";

    private final ObjectMapper objectMapper;
    private final Map<String, Resource<?>> resources = new LinkedHashMap<>();

    public MemberExtensionService(ObjectMapper objectMapper,
            AiMemberPointsRuleMapper memberPointsRuleMapper,
            AiMemberSignRecordMapper memberSignRecordMapper,
            AiMemberTagMapper memberTagMapper,
            AiMemberGrowthLogMapper memberGrowthLogMapper) {
        this.objectMapper = objectMapper;
        register("points-rule", AiMemberPointsRule.class, memberPointsRuleMapper, true, "rule_code", "rule_name", "biz_type", "remark");
        register("sign-record", AiMemberSignRecord.class, memberSignRecordMapper, false, "remark");
        register("tag", AiMemberTag.class, memberTagMapper, true, "tag_code", "tag_name", "tag_type", "remark");
        register("growth-log", AiMemberGrowthLog.class, memberGrowthLogMapper, false, "biz_type", "remark");
    }

    public Map<String, Object> list(String resource, Long tenantId, String keyword) {
        Resource<?> config = getResource(resource);
        QueryWrapper<?> wrapper = new QueryWrapper<>().eq("tenant_id", tenantId);
        applyKeyword(wrapper, config, keyword);
        wrapper.orderByDesc("id").last("LIMIT 500");
        List<?> rows = rawMapper(config.mapper()).selectList(raw(wrapper));
        return Map.of("total", rows.size(), "list", rows);
    }

    public Object get(String resource, Long id, Long tenantId) {
        Resource<?> config = getResource(resource);
        QueryWrapper<?> wrapper = new QueryWrapper<>().eq("id", id).eq("tenant_id", tenantId).last("LIMIT 1");
        return rawMapper(config.mapper()).selectOne(raw(wrapper));
    }

    public Object save(String resource, Map<String, Object> body) {
        Resource<?> config = getResource(resource);
        Long tenantId = toLong(body.getOrDefault("tenantId", 1));
        Long id = toLong(body.get("id"));
        Object entity = objectMapper.convertValue(body, config.entityClass());
        setAuditFields(entity, tenantId, id == null);
        if (id == null) {
            rawMapper(config.mapper()).insert(entity);
        } else {
            rawMapper(config.mapper()).updateById(entity);
        }
        Long savedId = id == null ? readId(entity) : id;
        return get(resource, savedId, tenantId);
    }

    public boolean updateStatus(String resource, Map<String, Object> body) {
        Resource<?> config = getResource(resource);
        if (!config.statusSupported()) {
            throw new IllegalArgumentException("当前资源不支持状态变更");
        }
        Long tenantId = toLong(body.getOrDefault("tenantId", 1));
        Long id = toLong(body.get("id"));
        if (id == null || body.get(STATUS_FIELD) == null) {
            throw new IllegalArgumentException("缺少状态变更参数");
        }
        Object entity = objectMapper.convertValue(Map.of("id", id, "tenantId", tenantId, STATUS_FIELD, body.get(STATUS_FIELD), "updateTime", LocalDateTime.now()), config.entityClass());
        rawMapper(config.mapper()).updateById(entity);
        return true;
    }

    public boolean delete(String resource, Long id, Long tenantId) {
        Resource<?> config = getResource(resource);
        QueryWrapper<?> wrapper = new QueryWrapper<>().eq("id", id).eq("tenant_id", tenantId);
        rawMapper(config.mapper()).delete(raw(wrapper));
        return true;
    }

    private <T> void register(String key, Class<T> entityClass, BaseMapper<T> mapper, boolean statusSupported, String... searchColumns) {
        resources.put(key, new Resource<>(entityClass, mapper, statusSupported, searchColumns));
    }

    private Resource<?> getResource(String key) {
        Resource<?> resource = resources.get(key);
        if (resource == null) {
            throw new IllegalArgumentException("未知扩展资源: " + key);
        }
        return resource;
    }

    private void applyKeyword(QueryWrapper<?> wrapper, Resource<?> config, String keyword) {
        if (!StringUtils.hasText(keyword) || config.searchColumns().length == 0) {
            return;
        }
        wrapper.and(nested -> {
            for (int i = 0; i < config.searchColumns().length; i++) {
                if (i > 0) {
                nested.or();
            }
                nested.like(config.searchColumns()[i], keyword);
            }
        });
    }

    private void setAuditFields(Object entity, Long tenantId, boolean create) {
        invokeSetter(entity, SET_TENANT_ID_METHOD, Long.class, tenantId);
        if (create) {
            invokeSetter(entity, SET_CREATE_TIME_METHOD, LocalDateTime.class, LocalDateTime.now());
        }
        invokeSetter(entity, SET_UPDATE_TIME_METHOD, LocalDateTime.class, LocalDateTime.now());
    }

    private Long readId(Object entity) {
        try {
            Object value = entity.getClass().getMethod("getId").invoke(entity);
            return value == null ? null : ((Number) value).longValue();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("读取实体ID失败", ex);
        }
    }

    private void invokeSetter(Object target, String method, Class<?> type, Object value) {
        try {
            target.getClass().getMethod(method, type).invoke(target, value);
        } catch (NoSuchMethodException ignored) {
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("设置实体字段失败: " + method, ex);
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private QueryWrapper raw(QueryWrapper<?> wrapper) {
        return (QueryWrapper) wrapper;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private BaseMapper rawMapper(BaseMapper<?> mapper) {
        return (BaseMapper) mapper;
    }

    private record Resource<T>(Class<T> entityClass, BaseMapper<T> mapper, boolean statusSupported, String[] searchColumns) {
    }
}
