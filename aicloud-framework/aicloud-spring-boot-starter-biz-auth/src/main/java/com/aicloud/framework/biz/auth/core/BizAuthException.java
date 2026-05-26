package com.aicloud.framework.biz.auth.core;

/**
 * Business auth exception.
 *
 * @author yifei
 */
public class BizAuthException extends RuntimeException {

    private final int code;

    public BizAuthException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
