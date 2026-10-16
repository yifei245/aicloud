package com.aicloud.framework.xss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;

/**
 * Xss 配置属性
 *
 * @author 芋道源码
 */
@ConfigurationProperties(prefix = "aicloud.xss")
@Validated
@Data
public class XssProperties {

    /**
     * 是否开启，默认为 true
     */
    private boolean enable = true;
    /**
     * 需要排除的 URL，默认为空
     */
    private List<String> excludeUrls = List.of(
            "/admin-api/bpm/model/create", "/admin-api/bpm/model/update", "/admin-api/bpm/model/import",
            "/admin-api/bpm/form/create", "/admin-api/bpm/form/update",
            "/admin-api/pay/order/notify/**", "/admin-api/pay/refund/notify/**", "/admin-api/pay/transfer/notify/**",
            "/admin-api/im/livekit/webhook", "/admin-api/mp/message/receive");

    /** Credentials and machine-readable process/SQL content are validated by their owning services. */
    private List<String> opaqueFields = List.of("password", "oldPassword", "newPassword", "clientSecret", "secret",
            "apiKey", "apiSecret", "accessToken", "refreshToken", "signature", "bpmnXml", "sql");

}
