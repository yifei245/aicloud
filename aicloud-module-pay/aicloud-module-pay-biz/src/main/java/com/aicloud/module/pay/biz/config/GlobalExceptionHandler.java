package com.aicloud.module.pay.biz.config;

import com.aicloud.module.pay.biz.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(400);
        response.setMsg(ex.getMessage());
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleInvalid(MethodArgumentNotValidException ex) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setCode(400);
        response.setMsg(ex.getBindingResult().getFieldError() == null ? "参数校验失败" : ex.getBindingResult().getFieldError().getDefaultMessage());
        return response;
    }
}
