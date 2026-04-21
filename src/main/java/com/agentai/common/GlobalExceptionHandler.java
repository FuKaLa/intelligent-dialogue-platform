package com.agentai.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult<?> handleException(Exception e) {
        // 记录异常信息
        e.printStackTrace();
        // 返回错误响应
        return ResponseResult.error(e.getMessage());
    }
}