package com.aicloud.common.pojo;

/**
 * Common API response body.
 *
 * @author yifei
 */
public class ApiResponse<T> {

    public static final int SUCCESS_CODE = 0;
    public static final String SUCCESS_MESSAGE = "OK";

    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(SUCCESS_CODE);
        response.setMsg(SUCCESS_MESSAGE);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> fail(int code, String msg) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMsg(msg);
        response.setData(null);
        return response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
