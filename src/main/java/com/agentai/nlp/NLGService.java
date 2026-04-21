package com.agentai.nlp;

import com.agentai.common.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * NLG回复生成服务
 * 使用真实的LLM生成自然语言回复
 */
@Component
public class NLGService {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    /**
     * 生成回复
     * @param intent 意图
     * @param entities 实体信息
     * @param context 对话上下文
     * @return 生成的回复
     */
    public String generateResponse(String intent, Map<String, Object> entities, String context) {
        // 使用LLM生成回复
        return generateFromLLM(intent, entities, context);
    }
    
    /**
     * 从LLM生成回复
     * @param intent 意图
     * @param entities 实体信息
     * @param context 对话上下文
     * @return 生成的回复
     */
    private String generateFromLLM(String intent, Map<String, Object> entities, String context) {
        // 构建回复生成的提示词模板
        String promptTemplate = "请根据以下信息生成一个自然、友好、专业的回复：\n" +
                "意图：{intent}\n" +
                "实体：{entities}\n" +
                "对话上下文：{context}\n" +
                "要求：\n" +
                "1. 回复要符合用户的意图\n" +
                "2. 要考虑对话历史上下文\n" +
                "3. 语言要自然流畅\n" +
                "4. 不要包含任何虚假信息\n" +
                "5. 如果信息不足，要礼貌地询问用户\n";
        
        // 构建参数
        Map<String, Object> params = Map.of(
                "intent", intent,
                "entities", entities,
                "context", context
        );
        
        // 调用LLM生成回复
        return openAIClient.call(promptTemplate, params);
    }
    
    /**
     * 生成回复（带检索结果）
     * @param intent 意图
     * @param entities 实体信息
     * @param context 对话上下文
     * @param searchResults 检索结果
     * @return 生成的回复
     */
    public String generateResponseWithSearch(String intent, Map<String, Object> entities, 
                                           String context, Map<String, Object> searchResults) {
        // 构建回复生成的提示词模板
        String promptTemplate = "请根据以下信息生成一个自然、友好、专业的回复：\n" +
                "意图：{intent}\n" +
                "实体：{entities}\n" +
                "对话上下文：{context}\n" +
                "检索结果：{searchResults}\n" +
                "要求：\n" +
                "1. 回复要符合用户的意图\n" +
                "2. 要考虑对话历史上下文\n" +
                "3. 要利用检索结果中的相关信息\n" +
                "4. 语言要自然流畅\n" +
                "5. 不要包含任何虚假信息\n" +
                "6. 如果信息不足，要礼貌地询问用户\n";
        
        // 构建参数
        Map<String, Object> params = Map.of(
                "intent", intent,
                "entities", entities,
                "context", context,
                "searchResults", searchResults
        );
        
        // 调用LLM生成回复
        return openAIClient.call(promptTemplate, params);
    }
}