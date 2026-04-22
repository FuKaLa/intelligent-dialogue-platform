package com.agentai.common;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * OpenAI客户端
 * 用于调用LLM服务
 */
@Component
public class OpenAIClient {
    
    private static final String API_URL = "http://10.8.0.54:6003/v1/chat/completions";
    private static final String API_KEY = "dev-key";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 调用LLM服务
     * @param promptTemplate 提示模板
     * @param params 参数
     * @return LLM响应
     */
    public String call(String promptTemplate, Map<String, Object> params) {
        try {
            // 替换模板中的参数
            String prompt = promptTemplate;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                prompt = prompt.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
            
            final String finalPrompt = prompt;
            
            // 构建请求体
            Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", java.util.Arrays.asList(
                    new java.util.HashMap<String, Object>() {{ put("role", "system"); put("content", "你是一个智能对话助手"); }},
                    new java.util.HashMap<String, Object>() {{ put("role", "user"); put("content", finalPrompt); }}
            ));
            requestBody.put("temperature", 0.7);
            
            // 发送请求
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setDoOutput(true);
            
            // 写入请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = objectMapper.writeValueAsBytes(requestBody);
                os.write(input, 0, input.length);
            }
            
            // 读取响应
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                // 解析响应
                Map<String, Object> responseMap = objectMapper.readValue(response.toString(), Map.class);
                java.util.List<Map<String, Object>> choices = (java.util.List<Map<String, Object>>) responseMap.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("LLM调用失败: " + e.getMessage());
        }
        
        return "";
    }
}
