package com.aicloud.common.exception;

/**
 * Common error code contract.
 *
 * @author yifei
 */
public enum ErrorCode {

    /**
     * Bad request.
     */
    BAD_REQUEST(400, "请求参数错误"),

    /**
     * Unauthorized request.
     */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /**
     * Forbidden request.
     */
    FORBIDDEN(403, "没有访问权限"),

    /**
     * Resource not found.
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * Internal server error.
     */
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
