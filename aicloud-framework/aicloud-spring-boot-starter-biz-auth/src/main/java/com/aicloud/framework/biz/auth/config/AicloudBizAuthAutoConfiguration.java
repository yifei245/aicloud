package com.aicloud.framework.biz.auth.config;

import com.aicloud.framework.biz.auth.core.BizAuthService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Business auth auto configuration.
 *
 * @author yifei
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "aicloud.biz-auth", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AicloudBizAuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BizAuthService bizAuthService() {
        return new BizAuthService();
    }
}
