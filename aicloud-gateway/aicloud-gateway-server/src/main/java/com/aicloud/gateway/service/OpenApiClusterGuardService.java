package com.aicloud.gateway.service;

import com.aicloud.gateway.config.OpenApiSecurityProperties;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class OpenApiClusterGuardService {

    private static final String NONCE_KEY_PREFIX = "aicloud:openapi:nonce:";
    private static final String RATE_KEY_PREFIX = "aicloud:openapi:rate:";
    private static final DefaultRedisScript<List> RATE_LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1]) "
                    + "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end "
                    + "local ttl = redis.call('TTL', KEYS[1]) "
                    + "return {current, ttl}", List.class);

    private final OpenApiSecurityProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, Long> localNonceUsedMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalCounter> localRateLimitMap = new ConcurrentHashMap<>();

    public OpenApiClusterGuardService(OpenApiSecurityProperties properties,
                                      org.springframework.beans.factory.ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.properties = properties;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    public boolean isReplayRequest(String appKey, String nonce, long nowSeconds) {
        long ttlSeconds = Math.max(
                Math.max(properties.getNonceTtlSeconds(), properties.getMinNonceWindowSeconds()), 1);
        if (properties.isRedisGuardEnabled() && redisTemplate != null) {
            String key = NONCE_KEY_PREFIX + appKey + ":" + nonce;
            try {
                Boolean success = redisTemplate.opsForValue().setIfAbsent(
                        key, "1", ttlSeconds, TimeUnit.SECONDS);
                return !Boolean.TRUE.equals(success);
            } catch (DataAccessException ignored) {
                // Fallback to local memory guard.
            }
        }
        cleanupLocalNonceCache(nowSeconds);
        String nonceKey = appKey + ":" + nonce;
        Long expireAt = localNonceUsedMap.putIfAbsent(
                nonceKey, nowSeconds + ttlSeconds);
        return expireAt != null;
    }

    public RateLimitDecision checkRateLimit(String appKey) {
        int limit = Math.max(properties.getRequestsPerMinute(), 1);
        long minuteWindow = Instant.now().getEpochSecond() / 60;
        long nowSeconds = Instant.now().getEpochSecond();
        if (properties.isRedisGuardEnabled() && redisTemplate != null) {
            String key = RATE_KEY_PREFIX + appKey + ":" + minuteWindow;
            try {
                List result = redisTemplate.execute(RATE_LIMIT_SCRIPT, List.of(key), "70");
                long count = parseLong(result, 0, 1L);
                long ttl = Math.max(parseLong(result, 1, 1L), 1L);
                long remaining = Math.max(limit - count, 0L);
                long resetAt = nowSeconds + ttl;
                if (count > limit) {
                    return RateLimitDecision.denied(limit, 0, resetAt);
                }
                return RateLimitDecision.allowed(limit, remaining, resetAt);
            } catch (DataAccessException ignored) {
                // Fallback to local memory guard.
            }
        }
        LocalCounter counter = localRateLimitMap.compute(appKey, (k, existing) -> {
            if (existing == null || existing.window != minuteWindow) {
                return new LocalCounter(minuteWindow, new AtomicInteger(0));
            }
                return existing;
        });
        int count = counter.counter.incrementAndGet();
        long resetAt = (minuteWindow + 1) * 60;
        long remaining = Math.max(limit - count, 0L);
        if (count > limit) {
            return RateLimitDecision.denied(limit, 0, resetAt);
        }
        return RateLimitDecision.allowed(limit, remaining, resetAt);
    }

    private long parseLong(List result, int idx, long defaultVal) {
        if (result == null || idx >= result.size() || result.get(idx) == null) {
            return defaultVal;
        }
        Object val = result.get(idx);
        if (val instanceof Number num) {
            return num.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(val));
        } catch (NumberFormatException ex) {
            return defaultVal;
        }
    }

    private void cleanupLocalNonceCache(long nowSeconds) {
        localNonceUsedMap.entrySet().removeIf(entry -> entry.getValue() <= nowSeconds);
    }

    private static final class LocalCounter {
        private final long window;
        private final AtomicInteger counter;

        private LocalCounter(long window, AtomicInteger counter) {
            this.window = window;
            this.counter = counter;
        }
    }

    public record RateLimitDecision(boolean allowed, int limit, long remaining, long resetAt) {
        public static RateLimitDecision allowed(int limit, long remaining, long resetAt) {
            return new RateLimitDecision(true, limit, remaining, resetAt);
        }

        public static RateLimitDecision denied(int limit, long remaining, long resetAt) {
            return new RateLimitDecision(false, limit, remaining, resetAt);
        }
    }
}
