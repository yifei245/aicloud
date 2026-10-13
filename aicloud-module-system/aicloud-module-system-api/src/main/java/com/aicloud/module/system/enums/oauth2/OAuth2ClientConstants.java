package com.aicloud.module.system.enums.oauth2;

/**
 * OAuth2.0 客户端的通用枚举
 *
 * @author 芋道源码
 */
public interface OAuth2ClientConstants {

    String CLIENT_ID_DEFAULT = "default";

    static String memberClientId(Integer terminal) {
        if (terminal == null) return "member-web";
        return switch (terminal) {
            case 10 -> "member-miniapp";
            case 11 -> "member-wechat";
            case 20 -> "member-h5";
            case 31 -> "member-app";
            default -> "member-web";
        };
    }

}
