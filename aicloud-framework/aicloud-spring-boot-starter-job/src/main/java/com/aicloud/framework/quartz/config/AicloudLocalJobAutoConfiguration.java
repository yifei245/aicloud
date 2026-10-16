package com.aicloud.framework.quartz.config;
import com.aicloud.framework.quartz.core.LocalXxlJobScheduler;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
@AutoConfiguration(afterName="org.redisson.spring.starter.RedissonAutoConfigurationV2")
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean(RedissonClient.class)
@ConditionalOnProperty(name="xxl.job.enabled",havingValue="false")
public class AicloudLocalJobAutoConfiguration {
    @Bean(destroyMethod="close")
    @ConditionalOnProperty(name="aicloud.job.local-enabled",havingValue="true",matchIfMissing=true)
    LocalXxlJobScheduler localXxlJobScheduler(ConfigurableApplicationContext context,RedissonClient redis,Environment environment) {
        return new LocalXxlJobScheduler(context,redis,environment);
    }
}
