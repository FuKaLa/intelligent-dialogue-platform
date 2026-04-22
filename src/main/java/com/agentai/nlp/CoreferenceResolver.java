package com.agentai.nlp;

import com.agentai.common.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 指代消解
 * 实现对话中的指代消解功能，使用LLM进行消解
 */
@Component
public class CoreferenceResolver {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    /**
     * 执行指代消解
     * @param text 当前文本
     * @param context 对话上下文
     * @return 消解后的文本
     */
    public String resolve(String text, String context) {
        String promptTemplate = "请根据对话上下文，将当前文本中的指代（如他、她、它、这、那等）替换为具体的实体。\n" +
                "上下文：{context}\n" +
                "当前文本：{text}\n" +
                "请返回消解后的文本，不要添加任何额外的说明或解释。";
        
        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("text", text);
        
        try {
            String result = openAIClient.call(promptTemplate, params);
            return result.trim();
        } catch (Exception e) {
            e.printStackTrace();
            // LLM调用失败，返回原始文本
            return text;
        }
    }
}
