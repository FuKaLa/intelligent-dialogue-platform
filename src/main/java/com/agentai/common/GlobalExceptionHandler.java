package com.agentai.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 处理系统中的各种异常，返回统一的错误格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     * @param e 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseResult<?>> handleBusinessException(BusinessException e) {
        ResponseResult<?> result = ResponseResult.error(e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理参数校验异常
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseResult<?>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ResponseResult<?> result = ResponseResult.error("参数校验失败", errors);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理绑定异常
     * @param e 绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResponseResult<?>> handleBindException(BindException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ResponseResult<?> result = ResponseResult.error("参数绑定失败", errors);
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理其他异常
     * @param e 其他异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseResult<?>> handleException(Exception e) {
        e.printStackTrace();
        ResponseResult<?> result = ResponseResult.error("系统内部错误: " + e.getMessage());
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
