package com.agentai.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI客户端实现
 * 用于调用本地LLM，使用OpenAI兼容API
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
     * @param prompt 提示词
     * @return 生成的回复
     */
    public String call(String prompt) {
        try {
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
            
            // 创建请求实体
            org.springframework.http.HttpEntity<Map<String, Object>> entity = 
                    new org.springframework.http.HttpEntity<>(requestBody, 
                            new org.springframework.http.HttpHeaders() {
                                {
                                    setAll(headers);
                                }
                            });
            
            // 发送请求
            Map<String, Object> response = restTemplate.postForObject(
                    baseUrl + "/chat/completions",
                    entity,
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
            throw new BusinessException("大模型返回结果解析失败");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // 记录异常
            e.printStackTrace();
            // 抛出业务异常
            throw new BusinessException("大模型调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 调用LLM生成回复（带参数）
     * @param template 提示词模板
     * @param params 模板参数
     * @return 生成的回复
     */
    public String call(String template, Map<String, Object> params) {
        try {
            // 替换模板参数
            String prompt = template;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                prompt = prompt.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
            
            // 调用大模型
            return call(prompt);
        } catch (Exception e) {
            // 记录异常
            e.printStackTrace();
            // 抛出业务异常
            throw new BusinessException("大模型调用失败: " + e.getMessage());
        }
    }
}