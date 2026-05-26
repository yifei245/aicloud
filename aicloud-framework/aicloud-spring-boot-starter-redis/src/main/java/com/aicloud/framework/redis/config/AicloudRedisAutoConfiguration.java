package com.aicloud.framework.redis.config;

import com.aicloud.framework.redis.core.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis auto configuration.
 *
 * @author yifei
 */
@AutoConfiguration
@ConditionalOnClass(StringRedisTemplate.class)
@EnableConfigurationProperties(AicloudRedisProperties.class)
@ConditionalOnProperty(prefix = "aicloud.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AicloudRedisAutoConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean
    public RedisCacheService redisCacheService(StringRedisTemplate redisTemplate,
                                               ObjectMapper objectMapper,
                                               AicloudRedisProperties properties) {
        return new RedisCacheService(redisTemplate, objectMapper, properties);
    }
}
