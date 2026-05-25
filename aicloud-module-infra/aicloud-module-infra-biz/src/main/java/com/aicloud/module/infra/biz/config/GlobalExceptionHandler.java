package com.aicloud.module.infra.biz.config;

import com.aicloud.module.infra.biz.model.ApiResponse;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/**
 * AICloud generated source.
 *
 * @author AICloud
 */
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, BindException.class, MethodArgumentNotValidException.class})
    public ApiResponse<Void> handleBadRequest(Exception ex) {
        return ApiResponse.fail(400, ex.getMessage());
    }
}
