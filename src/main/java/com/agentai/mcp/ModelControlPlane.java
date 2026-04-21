package com.agentai.mcp;

import com.agentai.common.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 模型调度中心（MCP）
 * 负责管理和调度各种AI模型的使用
 */
@Component
public class ModelControlPlane {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    /**
     * 调度模型执行任务
     */
    public String dispatch(String taskType, String input, Map<String, Object> params) {
        // 根据任务类型选择合适的模型和参数
        switch (taskType) {
            case "intent_recognition":
                return recognizeIntent(input);
            case "entity_extraction":
                return extractEntities(input);
            case "coreference_resolution":
                return resolveCoreference(input, (String) params.get("context"));
            case "condition_parsing":
                return parseCondition(input);
            case "nlg_generation":
                return generateResponse(input);
            default:
                return "Unknown task type: " + taskType;
        }
    }
    
    /**
     * 意图识别
     */
    private String recognizeIntent(String input) {
        String prompt = "请识别以下用户输入的意图，返回一个简洁的意图名称：\n" + input;
        return openAIClient.call(prompt);
    }
    
    /**
     * 实体抽取
     */
    private String extractEntities(String input) {
        String prompt = "请从以下文本中抽取实体，返回JSON格式：\n" + input;
        return openAIClient.call(prompt);
    }
    
    /**
     * 指代消解
     */
    private String resolveCoreference(String input, String context) {
        String prompt = "请解决以下文本中的指代关系，结合上下文：\n" +
                       "上下文：" + context + "\n" +
                       "当前文本：" + input;
        return openAIClient.call(prompt);
    }
    
    /**
     * 条件解析
     */
    private String parseCondition(String input) {
        String prompt = "请解析以下条件语句，返回结构化的条件表达式：\n" + input;
        return openAIClient.call(prompt);
    }
    
    /**
     * NLG回复生成
     */
    private String generateResponse(String input) {
        String prompt = "请根据以下内容生成一个自然的回复：\n" + input;
        return openAIClient.call(prompt);
    }
}