package com.agentai.controller;

/**
 * 对话请求参数
 */
public class ChatRequest {
    // 用户输入
    private String message;
    // 对话ID
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