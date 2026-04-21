package com.agentai.nlp;

import com.agentai.common.CacheService;
import com.agentai.common.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 意图识别
 */
@Component
public class IntentRecognizer {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    @Autowired
    private CacheService cacheService;
    
    /**
     * 识别用户意图
     * @param text 用户输入文本
     * @return 识别的意图
     */
    public String recognize(String text) {
        return recognizeWithContext(text, "");
    }
    
    /**
     * 识别用户意图（带上下文）
     * @param text 用户输入文本
     * @param context 对话上下文
     * @return 识别的意图
     */
    public String recognizeWithContext(String text, String context) {
        String cacheKey = text + "_" + context;
        
        String cachedIntent = cacheService.getCachedIntent(text, context);
        if (cachedIntent != null) {
            return cachedIntent;
        }
        
        String promptTemplate = "请根据以下对话上下文和用户输入，识别用户的意图，返回一个简洁的意图名称：\n" +
                "上下文：{context}\n" +
                "用户输入：{text}";
        
        Map<String, Object> params = Map.of(
                "context", context,
                "text", text
        );
        
        String result = openAIClient.call(promptTemplate, params);
        
        return result.trim();
    }
}
