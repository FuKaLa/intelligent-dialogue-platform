package com.agentai.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI客户端实现
 * 用于调用本地LLM
 */
@Component
public class OpenAIClient {
    
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.openai.chat.model}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 调用LLM生成回复
     */
    public String call(String prompt) {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.7);
        
        // 构建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        
        // 发送请求
        try {
            Map<String, Object> response = restTemplate.postForObject(
                baseUrl + "/chat/completions",
                requestBody,
                Map.class
            );
            
            // 解析响应
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                    return (String) messageObj.get("content");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "抱歉，我暂时无法回答您的问题";
    }
}