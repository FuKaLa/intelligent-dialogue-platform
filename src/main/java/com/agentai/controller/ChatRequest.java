package com.agentai.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 对话请求参数
 */
public class ChatRequest {
    
    @NotBlank(message = "消息不能为空")
    @Size(max = 1000, message = "消息长度不能超过1000个字符")
    private String message;
    
    @NotBlank(message = "对话ID不能为空")
    @Size(max = 100, message = "对话ID长度不能超过100个字符")
    private String conversationId;
    
    // getter和setter方法
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
