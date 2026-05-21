package com.aicloud.module.erp.biz.config;

import com.aicloud.module.erp.biz.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) { ApiResponse<Void> r=new ApiResponse<>(); r.setCode(400); r.setMsg(ex.getMessage()); return r; }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleInvalid(MethodArgumentNotValidException ex) { ApiResponse<Void> r=new ApiResponse<>(); r.setCode(400); r.setMsg(ex.getBindingResult().getFieldError()==null?"参数校验失败":ex.getBindingResult().getFieldError().getDefaultMessage()); return r; }
}
