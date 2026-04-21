package com.agentai.nlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.agentai.common.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实体抽取和槽位填充
 */
@Component
public class EntityExtractor {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    /**
     * 抽取实体和填充槽位
     */
    public Map<String, Object> extract(String text) {
        // 1. 使用HanLP进行初步的实体识别
        List<Term> terms = HanLP.segment(text);
        
        // 2. 构建提示，使用本地LLM进行更精确的实体抽取和槽位填充
        String prompt = "请从以下文本中抽取实体和填充槽位，返回JSON格式：\n" + text;
        
        // 调用本地LLM
        String response = openAIClient.call(prompt);
        
        // 这里简化处理，实际应该解析JSON响应
        // 为了演示，返回一个简单的Map
        return Map.of(
            "entities", terms.stream()
                .filter(term -> term.nature.startsWith("n") || term.nature.startsWith("v"))
                .collect(Collectors.toMap(term -> term.word, term -> term.nature.toString())),
            "slots", Map.of("content", text)
        );
    }
}