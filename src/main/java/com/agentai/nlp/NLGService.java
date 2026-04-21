package com.agentai.nlp;

import com.agentai.common.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * NLG回复生成服务
 */
@Component
public class NLGService {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    /**
     * 生成回复
     */
    public String generateResponse(String intent, Map<String, Object> entities, String context) {
        // 1. 尝试使用模板生成回复
        String templateResponse = generateFromTemplate(intent, entities);
        if (templateResponse != null) {
            return templateResponse;
        }
        
        // 2. 如果没有匹配的模板，使用本地LLM生成回复
        return generateFromLLM(intent, entities, context);
    }
    
    /**
     * 从模板生成回复
     */
    private String generateFromTemplate(String intent, Map<String, Object> entities) {
        // 简单的模板匹配
        if (intent.contains("天气")) {
            String city = (String) entities.get("city");
            if (city != null) {
                return "当前" + city + "的天气情况良好";
            }
        }
        return null;
    }
    
    /**
     * 从LLM生成回复
     */
    private String generateFromLLM(String intent, Map<String, Object> entities, String context) {
        // 构建提示
        String prompt = "请根据以下信息生成一个自然的回复：\n" +
                       "意图：" + intent + "\n" +
                       "实体：" + entities + "\n" +
                       "上下文：" + context;
        
        // 调用本地LLM
        return openAIClient.call(prompt);
    }
}