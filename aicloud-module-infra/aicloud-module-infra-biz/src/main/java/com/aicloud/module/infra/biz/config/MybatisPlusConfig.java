package com.aicloud.module.infra.biz.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.aicloud.module.infra.biz.mapper")
public class MybatisPlusConfig {
}
