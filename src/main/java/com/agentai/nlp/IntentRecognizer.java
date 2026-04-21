package com.agentai.nlp;

import com.agentai.common.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 意图识别
 */
@Component
public class IntentRecognizer {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    /**
     * 识别用户意图
     */
    public String recognize(String text) {
        // 构建意图识别的提示
        String prompt = "请识别以下用户输入的意图，返回一个简洁的意图名称：\n" + text;
        
        // 调用本地LLM进行意图识别
        return openAIClient.call(prompt);
    }
}