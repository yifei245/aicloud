package com.aicloud.framework.job.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Job framework properties.
 *
 * @author yifei
 */
@ConfigurationProperties(prefix = "aicloud.job")
public class AicloudJobProperties {

    private boolean enabled = true;
    private int corePoolSize = 4;
    private int maxPoolSize = 16;
    private int queueCapacity = 1000;
    private int schedulerPoolSize = 4;
    private String threadNamePrefix = "aicloud-job-";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getSchedulerPoolSize() {
        return schedulerPoolSize;
    }

    public void setSchedulerPoolSize(int schedulerPoolSize) {
        this.schedulerPoolSize = schedulerPoolSize;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }
}
