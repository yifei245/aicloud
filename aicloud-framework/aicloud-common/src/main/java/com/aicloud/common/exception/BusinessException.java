package com.aicloud.common.exception;

/**
 * Business exception carrying a stable error code.
 *
 * @author yifei
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
