package com.aicloud.framework.job.config;

import com.aicloud.framework.job.core.JobRunner;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Job auto configuration.
 *
 * @author yifei
 */
@EnableAsync
@EnableScheduling
@AutoConfiguration
@EnableConfigurationProperties(AicloudJobProperties.class)
@ConditionalOnProperty(prefix = "aicloud.job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AicloudJobAutoConfiguration {

    @Bean("aicloudJobExecutor")
    @ConditionalOnMissingBean(name = "aicloudJobExecutor")
    public ThreadPoolTaskExecutor aicloudJobExecutor(AicloudJobProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }

    @Bean("aicloudTaskScheduler")
    @ConditionalOnMissingBean(name = "aicloudTaskScheduler")
    public ThreadPoolTaskScheduler aicloudTaskScheduler(AicloudJobProperties properties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getSchedulerPoolSize());
        scheduler.setThreadNamePrefix("aicloud-scheduler-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    @ConditionalOnMissingBean
    public JobRunner jobRunner(@Qualifier("aicloudJobExecutor") Executor executor) {
        return new JobRunner(executor);
    }
}
