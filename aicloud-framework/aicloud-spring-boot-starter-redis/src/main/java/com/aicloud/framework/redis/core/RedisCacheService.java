package com.aicloud.framework.redis.core;

import com.aicloud.framework.redis.config.AicloudRedisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * Shared Redis cache helper for business services.
 *
 * @author yifei
 */
public class RedisCacheService {

    private static final String KEY_SEPARATOR = ":";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AicloudRedisProperties properties;

    public RedisCacheService(StringRedisTemplate redisTemplate,
                             ObjectMapper objectMapper,
                             AicloudRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void set(String key, Object value) {
        set(key, value, properties.getDefaultTtl());
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            String payload = objectMapper.writeValueAsString(value);
            Duration safeTtl = ttl == null ? properties.getDefaultTtl() : ttl;
            redisTemplate.opsForValue().set(formatKey(key), payload, safeTtl.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Redis write failed", ex);
        }
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        String payload = redisTemplate.opsForValue().get(formatKey(key));
        if (!StringUtils.hasText(payload)) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(payload, type));
        } catch (Exception ex) {
            throw new IllegalStateException("Redis read failed", ex);
        }
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(formatKey(key));
    }

    public Long increment(String key, Duration ttl) {
        String actualKey = formatKey(key);
        Long value = redisTemplate.opsForValue().increment(actualKey);
        if (ttl != null && value != null && value <= 1L) {
            redisTemplate.expire(actualKey, ttl);
        }
        return value;
    }

    public String formatKey(String key) {
        if (!StringUtils.hasText(properties.getKeyPrefix())) {
            return key;
        }
        return properties.getKeyPrefix() + KEY_SEPARATOR + key;
    }
}
