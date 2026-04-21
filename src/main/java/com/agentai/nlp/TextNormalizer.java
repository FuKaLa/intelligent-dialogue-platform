package com.agentai.nlp;

import com.hankcs.hanlp.HanLP;
import org.springframework.stereotype.Component;

/**
 * 文本归一化处理
 */
@Component
public class TextNormalizer {
    
    /**
     * 对文本进行归一化处理
     */
    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 1. 去除首尾空格
        text = text.trim();
        
        // 2. 统一标点符号
        text = text.replaceAll("[，]", ",")
                  .replaceAll("[。]", ".")
                  .replaceAll("[！]", "!")
                  .replaceAll("[？]", "?")
                  .replaceAll("[；]", ";")
                  .replaceAll("[：]", ":");
        
        // 3. 使用HanLP进行分词和词性标注（可选）
        // 这里可以根据需要添加更多的归一化处理
        
        return text;
    }
}