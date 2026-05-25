package com.aicloud.module.erp.biz.model;
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class ApiResponse<T> { private int code; private String msg; private T data; public static <T> ApiResponse<T> ok(T data) { ApiResponse<T> r=new ApiResponse<>(); r.setCode(0); r.setMsg("success"); r.setData(data); return r; } public int getCode(){return code;} public void setCode(int v){code=v;} public String getMsg(){return msg;} public void setMsg(String v){msg=v;} public T getData(){return data;} public void setData(T v){data=v;} }
