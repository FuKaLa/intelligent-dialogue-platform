package com.agentai.nlp;

import com.agentai.common.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 指代消解
 */
@Component
public class CoreferenceResolver {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    /**
     * 解决指代关系
     */
    public String resolve(String text, String context) {
        // 构建提示，使用本地LLM进行指代消解
        String prompt = "请解决以下文本中的指代关系，结合上下文：\n" +
                       "上下文：" + context + "\n" +
                       "当前文本：" + text;
        
        // 调用本地LLM
        return openAIClient.call(prompt);
    }
}