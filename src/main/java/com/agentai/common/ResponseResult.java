package com.agentai.common;

import java.io.Serializable;

/**
 * 统一返回格式
 */
public class ResponseResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 状态码
    private int code;
    // 消息
    private String message;
    // 数据
    private T data;
    
    // 成功状态码
    public static final int SUCCESS_CODE = 200;
    // 失败状态码
    public static final int ERROR_CODE = 500;
    
    /**
     * 私有构造方法
     */
    private ResponseResult() {
    }
    
    /**
     * 成功响应
     */
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage("success");
        result.setData(data);
        return result;
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ResponseResult<T> success() {
        return success(null);
    }
    
    /**
     * 失败响应
     */
    public static <T> ResponseResult<T> error(String message) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setCode(ERROR_CODE);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
    
    // getter和setter方法
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
}