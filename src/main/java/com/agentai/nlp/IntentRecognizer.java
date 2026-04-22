package com.agentai.nlp;

import com.agentai.common.BusinessException;
import com.agentai.common.OpenAIClient;
import com.hankcs.hanlp.seg.common.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 意图识别器
 * 实现规则前置兜底+LLM泛化增强混合架构
 */
@Component
public class IntentRecognizer {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    @Autowired
    private TextNormalizer textNormalizer;
    
    // 意图关键词映射
    private static final Map<IntentEnum, List<String>> INTENT_KEYWORDS = new HashMap<>();
    
    // 意图正则表达式映射
    private static final Map<IntentEnum, Pattern> INTENT_PATTERNS = new HashMap<>();
    
    static {
        // 初始化意图关键词
        INTENT_KEYWORDS.put(IntentEnum.GREETING, Arrays.asList("你好", "您好", "早上好", "下午好", "晚上好", "嗨", "哈喽"));
        INTENT_KEYWORDS.put(IntentEnum.HELP, Arrays.asList("帮助", "帮忙", "怎么用", "使用方法", "教程"));
        INTENT_KEYWORDS.put(IntentEnum.WEATHER, Arrays.asList("天气", "气温", "温度", "下雨", "晴天", "多云"));
        INTENT_KEYWORDS.put(IntentEnum.CALCULATOR, Arrays.asList("计算", "加", "减", "乘", "除", "等于", "等于号"));
        INTENT_KEYWORDS.put(IntentEnum.SEARCH, Arrays.asList("搜索", "查找", "查询", "搜一下"));
        
        // 初始化意图正则表达式
        INTENT_PATTERNS.put(IntentEnum.WEATHER, Pattern.compile(".*天气.*"));
        INTENT_PATTERNS.put(IntentEnum.CALCULATOR, Pattern.compile(".*[+\\-*/].*"));
        INTENT_PATTERNS.put(IntentEnum.SEARCH, Pattern.compile(".*搜索.*|.*查找.*|.*查询.*"));
    }
    
    /**
     * 识别意图
     * @param text 用户输入文本
     * @return 意图识别结果
     */
    public Map<String, Object> recognize(String text) {
        return recognizeWithContext(text, "");
    }
    
    /**
     * 识别意图（带上下文）
     * @param text 用户输入文本
     * @param context 对话上下文
     * @return 意图识别结果
     */
    public Map<String, Object> recognizeWithContext(String text, String context) {
        // 1. 文本归一化
        String normalizedText = textNormalizer.normalize(text);
        
        // 2. 分词
        List<Term> terms = textNormalizer.segmentWithFilter(normalizedText);
        
        // 3. 提取关键词
        List<String> keywords = textNormalizer.extractKeywords(terms);
        
        // 4. 规则意图匹配
        Map<String, Object> ruleResult = matchRuleIntent(normalizedText, terms, keywords);
        String intent = (String) ruleResult.get("intent");
        double confidence = (double) ruleResult.get("confidence");
        
        // 5. 如果规则匹配成功且置信度高，直接返回
        if (!IntentEnum.OTHER.getCode().equals(intent) && confidence > 0.7) {
            return ruleResult;
        }
        
        // 6. 否则使用LLM进行意图补全/校验
        Map<String, Object> llmResult = recognizeWithLLM(normalizedText, context, ruleResult);
        
        // 7. 合并结果
        Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("intent", llmResult.get("intent"));
        finalResult.put("confidence", llmResult.get("confidence"));
        finalResult.put("slots", llmResult.get("slots"));
        
        return finalResult;
    }
    
    /**
     * 规则意图匹配
     * @param text 归一化文本
     * @param terms 分词结果
     * @param keywords 关键词列表
     * @return 规则匹配结果
     */
    private Map<String, Object> matchRuleIntent(String text, List<Term> terms, List<String> keywords) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 正则表达式匹配
        for (Map.Entry<IntentEnum, Pattern> entry : INTENT_PATTERNS.entrySet()) {
            if (entry.getValue().matcher(text).find()) {
                result.put("intent", entry.getKey().getCode());
                result.put("confidence", 0.9);
                result.put("slots", new HashMap<>());
                return result;
            }
        }
        
        // 2. 关键词匹配
        Map<IntentEnum, Integer> intentScores = new HashMap<>();
        
        // 统计每个意图的关键词命中次数
        for (Map.Entry<IntentEnum, List<String>> entry : INTENT_KEYWORDS.entrySet()) {
            IntentEnum intent = entry.getKey();
            List<String> intentKeywords = entry.getValue();
            int score = 0;
            
            // 检查文本中是否包含关键词
            for (String keyword : intentKeywords) {
                if (text.contains(keyword)) {
                    score++;
                }
            }
            
            // 检查分词结果中是否包含关键词
            for (Term term : terms) {
                if (intentKeywords.contains(term.word)) {
                    score++;
                }
            }
            
            // 检查关键词列表中是否包含意图关键词
            for (String keyword : keywords) {
                if (intentKeywords.contains(keyword)) {
                    score++;
                }
            }
            
            if (score > 0) {
                intentScores.put(intent, score);
            }
        }
        
        // 找到得分最高的意图
        if (!intentScores.isEmpty()) {
            IntentEnum bestIntent = Collections.max(intentScores.entrySet(), Map.Entry.comparingByValue()).getKey();
            int maxScore = intentScores.get(bestIntent);
            
            // 计算置信度
            double confidence = Math.min(1.0, maxScore / 3.0); // 最高3分，对应1.0置信度
            
            result.put("intent", bestIntent.getCode());
            result.put("confidence", confidence);
            result.put("slots", new HashMap<>());
            return result;
        }
        
        // 3. 无匹配，返回其他意图
        result.put("intent", IntentEnum.OTHER.getCode());
        result.put("confidence", 0.1);
        result.put("slots", new HashMap<>());
        return result;
    }
    
    /**
     * 使用LLM识别意图
     * @param text 归一化文本
     * @param context 对话上下文
     * @param ruleResult 规则匹配结果
     * @return LLM识别结果
     */
    private Map<String, Object> recognizeWithLLM(String text, String context, Map<String, Object> ruleResult) {
        String promptTemplate = "请根据以下对话上下文和用户输入，识别用户的意图。\n" +
                "上下文：{context}\n" +
                "用户输入：{text}\n" +
                "规则识别结果：{ruleResult}\n" +
                "请从以下意图中选择最合适的一个：greeting（问候）、help（帮助）、weather（天气查询）、calculator（计算）、search（搜索）、chitchat（闲聊）、other（其他）\n" +
                "请以JSON格式返回识别结果，包含以下字段：\n" +
                "- intent：意图代码\n" +
                "- confidence：置信度（0-1之间）\n" +
                "- slots：槽位信息（如城市、日期等）\n" +
                "例如：{\"intent\": \"weather\", \"confidence\": 0.95, \"slots\": {\"city\": \"北京\", \"date\": \"今天\"}}\n" +
                "请只返回JSON格式的识别结果，不要返回其他任何内容。";
        
        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("text", text);
        params.put("ruleResult", ruleResult);
        
        try {
            String result = openAIClient.call(promptTemplate, params);
            return parseLLMResponse(result);
        } catch (Exception e) {
            e.printStackTrace();
            // LLM调用失败，返回规则匹配结果
            return ruleResult;
        }
    }
    
    /**
     * 解析LLM返回的响应
     * @param response LLM返回的响应
     * @return 解析后的意图识别结果
     */
    private Map<String, Object> parseLLMResponse(String response) {
        // 简单的JSON解析，实际项目中可以使用JSON库
        Map<String, Object> result = new HashMap<>();
        
        // 去除多余的文字
        response = response.trim();
        if (response.startsWith("{") && response.endsWith("}")) {
            try {
                // 这里使用简化的解析，实际项目中应该使用JSON库
                // 提取intent
                int intentStart = response.indexOf("\"intent\":") + 9;
                int intentEnd = response.indexOf("\"", intentStart + 1);
                String intent = response.substring(intentStart + 1, intentEnd);
                
                // 提取confidence
                int confidenceStart = response.indexOf("\"confidence\":") + 13;
                int confidenceEnd = response.indexOf(",", confidenceStart);
                if (confidenceEnd == -1) {
                    confidenceEnd = response.indexOf("}", confidenceStart);
                }
                double confidence = Double.parseDouble(response.substring(confidenceStart, confidenceEnd).trim());
                
                // 提取slots
                Map<String, Object> slots = new HashMap<>();
                int slotsStart = response.indexOf("\"slots\":") + 8;
                if (slotsStart > 8) {
                    int slotsEnd = response.lastIndexOf("}");
                    String slotsStr = response.substring(slotsStart, slotsEnd);
                    // 简单解析slots
                    if (!"{}".equals(slotsStr.trim())) {
                        // 这里可以添加更复杂的slots解析逻辑
                    }
                }
                
                result.put("intent", intent);
                result.put("confidence", confidence);
                result.put("slots", slots);
            } catch (Exception e) {
                e.printStackTrace();
                // 解析失败，返回其他意图
                result.put("intent", IntentEnum.OTHER.getCode());
                result.put("confidence", 0.1);
                result.put("slots", new HashMap<>());
            }
        } else {
            // 不是JSON格式，返回其他意图
            result.put("intent", IntentEnum.OTHER.getCode());
            result.put("confidence", 0.1);
            result.put("slots", new HashMap<>());
        }
        
        return result;
    }
}
