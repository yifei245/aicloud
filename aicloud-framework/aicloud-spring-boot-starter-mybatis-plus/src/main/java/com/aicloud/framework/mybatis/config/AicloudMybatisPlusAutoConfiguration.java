package com.aicloud.framework.mybatis.config;

import com.aicloud.framework.tenant.config.TenantProperties;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis Plus shared auto configuration.
 *
 * @author yifei
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class AicloudMybatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AicloudTenantLineHandler aicloudTenantLineHandler(TenantProperties tenantProperties) {
        return new AicloudTenantLineHandler(tenantProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(AicloudTenantLineHandler tenantLineHandler) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(tenantLineHandler));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
