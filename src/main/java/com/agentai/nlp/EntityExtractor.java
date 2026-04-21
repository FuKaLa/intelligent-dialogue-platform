package com.agentai.nlp;

import com.agentai.common.CacheService;
import com.agentai.common.OpenAIClient;
import com.agentai.common.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 实体抽取和槽位填充
 */
@Component
public class EntityExtractor {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    @Autowired
    private CacheService cacheService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 清理 LLM 响应，去除 Markdown 代码块标记
     * @param response LLM 原始响应
     * @return 清理后的 JSON 字符串
     */
    private String cleanResponse(String response) {
        if (response == null || response.isEmpty()) {
            return response;
        }
        
        String cleaned = response.trim();
        
        // 去除开头的 ```json 或 ```
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        // 去除结尾的 ```
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        return cleaned.trim();
    }
    
    /**
     * 抽取实体和填充槽位
     * @param text 用户输入文本
     * @return 实体和槽位信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> extract(String text) {
        return extractWithContext(text, "");
    }
    
    /**
     * 抽取实体和填充槽位（带上下文）
     * @param text 用户输入文本
     * @param context 对话上下文
     * @return 实体和槽位信息
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> extractWithContext(String text, String context) {
        Map<String, Object> cachedEntities = cacheService.getCachedEntities(text, context);
        if (cachedEntities != null) {
            return cachedEntities;
        }
        
        String promptTemplate = "请根据以下对话上下文和用户输入，抽取实体和填充槽位，返回JSON格式，包含entities和slots两个字段：\n" +
                "上下文：{context}\n" +
                "用户输入：{text}";
        
        Map<String, Object> params = Map.of(
                "context", context,
                "text", text
        );
        
        String response = openAIClient.call(promptTemplate, params);
        
        try {
            String cleanedResponse = cleanResponse(response);
            return objectMapper.readValue(cleanedResponse, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("实体抽取失败: " + e.getMessage());
        }
    }
}
