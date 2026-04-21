package com.agentai.mcp;

import com.agentai.common.CacheService;
import com.agentai.common.ElasticsearchClient;
import com.agentai.common.MilvusVectorClient;
import com.agentai.common.OpenAIClient;
import com.agentai.dst.DialogueStateManager;
import com.agentai.nlp.EntityExtractor;
import com.agentai.nlp.IntentRecognizer;
import com.agentai.skill.SkillManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型调度中心（MCP）
 * 负责管理和调度各种AI模型的使用，基于意图、槽位和上下文做出决策
 * 
 * <p>核心功能：
 * 1. 处理对话请求，协调各个组件的工作
 * 2. 管理对话历史和状态
 * 3. 执行语义检索
 * 4. 调用技能执行器
 * 5. 优化 LLM 调用，提高系统性能
 * </p>
 * 
 * <p>工作流程：
 * 1. 获取对话历史
 * 2. 识别用户意图（使用缓存）
 * 3. 抽取实体和槽位（使用缓存）
 * 4. 执行语义检索（使用缓存）
 * 5. 判断是否需要执行技能
 * 6. 生成回复（优化 LLM 调用）
 * 7. 更新对话历史
 * </p>
 */
@Component
public class ModelControlPlane {
    
    /**
     * OpenAI 客户端，用于调用 LLM 生成回复
     */
    @Autowired
    private OpenAIClient openAIClient;
    
    /**
     * 意图识别器，用于识别用户意图
     */
    @Autowired
    private IntentRecognizer intentRecognizer;
    
    /**
     * 实体提取器，用于提取文本中的实体和槽位
     */
    @Autowired
    private EntityExtractor entityExtractor;
    
    /**
     * 对话状态管理器，用于管理对话历史和状态
     */
    @Autowired
    private DialogueStateManager dialogueStateManager;
    
    /**
     * Milvus 向量客户端，用于向量存储和检索
     */
    @Autowired
    private MilvusVectorClient milvusVectorClient;
    
    /**
     * Elasticsearch 客户端，用于文本检索
     */
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    
    /**
     * 技能管理器，用于执行特定技能
     */
    @Autowired
    private SkillManager skillManager;
    
    /**
     * 缓存服务，用于缓存各种计算结果
     */
    @Autowired
    private CacheService cacheService;
    
    /**
     * 处理对话请求
     * 
     * @param conversationId 对话ID
     * @param userInput 用户输入
     * @return 处理结果，包含意图、槽位、检索结果、技能执行结果和回复
     * 
     * <p>处理流程：
     * 1. 获取对话历史
     * 2. 识别用户意图（使用缓存）
     * 3. 抽取实体和槽位（使用缓存）
     * 4. 执行语义检索（使用缓存）
     * 5. 判断是否需要执行技能
     * 6. 生成回复（优化 LLM 调用）
     * 7. 更新对话历史
     * </p>
     */
    public Map<String, Object> processDialogue(String conversationId, String userInput) {
        // 1. 获取对话历史
        String history = dialogueStateManager.getHistory(conversationId);
        
        // 2. 识别意图（使用缓存）
        String intent = intentRecognizer.recognizeWithContext(userInput, history);
        dialogueStateManager.updateIntent(conversationId, intent);
        
        // 3. 抽取实体和槽位（使用缓存）
        Map<String, Object> nluResult = entityExtractor.extractWithContext(userInput, history);
        Map<String, Object> slots = (Map<String, Object>) nluResult.get("slots");
        if (slots == null) {
            slots = new HashMap<>();
        }
        dialogueStateManager.updateSlots(conversationId, slots);
        
        // 4. 语义检索（暂时只使用 Elasticsearch，使用缓存）
        List<Map<String, Object>> searchResults = semanticSearch(userInput);
        
        // 5. 执行技能（如果需要）
        String skillResult = null;
        if (shouldExecuteSkill(intent, slots)) {
            Map<String, Object> skillParams = new HashMap<>();
            skillParams.putAll(slots);
            skillParams.put("input", userInput);
            skillResult = skillManager.executeSkill(intent, skillParams);
        }
        
        // 6. 生成回复（合并决策和回复生成，减少 LLM 调用）
        String response = generateResponse(intent, slots, searchResults, history, skillResult);
        
        // 7. 更新对话历史
        dialogueStateManager.addHistory(conversationId, userInput, response);
        
        // 使用 HashMap 避免 Map.of() 的 null 值问题
        Map<String, Object> result = new HashMap<>();
        result.put("intent", intent);
        result.put("slots", slots);
        result.put("searchResults", searchResults);
        result.put("skillResult", skillResult);
        result.put("response", response);
        return result;
    }
    
    /**
     * 语义检索（暂时只使用 Elasticsearch）
     * 
     * @param query 查询文本
     * @return 检索结果列表
     * 
     * <p>检索流程：
     * 1. 尝试从缓存获取检索结果
     * 2. 缓存未命中时，执行 Elasticsearch 检索
     * 3. 将检索结果存入缓存
     * 4. 返回检索结果
     * </p>
     */
    private List<Map<String, Object>> semanticSearch(String query) {
        try {
            // 尝试从缓存获取
            List<Map<String, Object>> cachedResults = cacheService.getCachedSearchResults(query);
            if (cachedResults != null) {
                return cachedResults;
            }
            
            // 缓存未命中，执行检索
            List<Map<String, Object>> results = elasticsearchClient.searchDocuments(query, 5);
            // 存入缓存
            cacheService.cacheSearchResults(query, results);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * 判断是否需要执行技能
     * 
     * @param intent 意图
     * @param slots 槽位
     * @return 是否需要执行技能
     * 
     * <p>判断逻辑：
     * 1. 检查意图是否为 null
     * 2. 将意图转换为小写进行匹配
     * 3. 匹配常见的需要执行技能的意图类型
     * </p>
     */
    private boolean shouldExecuteSkill(String intent, Map<String, Object> slots) {
        // 根据意图和槽位判断是否需要执行技能
        if (intent == null) {
            return false;
        }
        
        String intentLower = intent.toLowerCase();
        return intentLower.contains("计算") || 
               intentLower.contains("addition") || 
               intentLower.contains("subtraction") ||
               intentLower.contains("multiplication") ||
               intentLower.contains("division") ||
               intentLower.contains("天气") ||
               intentLower.contains("weather") ||
               intentLower.contains("查询") ||
               intentLower.contains("search");
    }
    
    /**
     * 生成回复（合并决策和回复生成，减少 LLM 调用）
     * 
     * @param intent 意图
     * @param slots 槽位
     * @param searchResults 检索结果
     * @param history 对话历史
     * @param skillResult 技能执行结果
     * @return 生成的回复
     * 
     * <p>优化策略：
     * 1. 优先使用技能执行结果生成回复（减少 LLM 调用）
     * 2. 其次使用检索结果生成回复
     * 3. 最后使用意图和上下文生成回复
     * </p>
     * 
     * <p>LLM 调用优化：
     * - 合并决策和回复生成，减少 LLM 调用次数
     * - 只在必要时调用 LLM
     * - 提供详细的上下文信息，提高回复质量
     * </p>
     */
    private String generateResponse(String intent, Map<String, Object> slots, 
                                   List<Map<String, Object>> searchResults, 
                                   String history, String skillResult) {
        // 如果有技能执行结果，直接返回（减少 LLM 调用）
        if (skillResult != null && !skillResult.isEmpty()) {
            String promptTemplate = "请根据以下技能执行结果生成一个自然、友好的回复：\n" +
                    "技能执行结果：{skillResult}";
            
            Map<String, Object> params = new HashMap<>();
            params.put("skillResult", skillResult);
            return openAIClient.call(promptTemplate, params);
        }
        
        // 如果有检索结果，基于检索结果生成回复
        if (searchResults != null && !searchResults.isEmpty()) {
            String promptTemplate = "请根据以下信息生成一个自然、友好的回复：\n" +
                    "意图：{intent}\n" +
                    "槽位：{slots}\n" +
                    "检索结果：{searchResults}\n" +
                    "对话历史：{history}";
            
            Map<String, Object> params = new HashMap<>();
            params.put("intent", intent != null ? intent : "");
            params.put("slots", slots != null ? slots : new HashMap<>());
            params.put("searchResults", searchResults);
            params.put("history", history != null ? history : "");
            return openAIClient.call(promptTemplate, params);
        }
        
        // 默认情况：基于意图和上下文生成回复
        String promptTemplate = "请根据以下信息生成一个自然、友好的回复：\n" +
                "意图：{intent}\n" +
                "槽位：{slots}\n" +
                "对话历史：{history}";
        
        Map<String, Object> params = new HashMap<>();
        params.put("intent", intent != null ? intent : "");
        params.put("slots", slots != null ? slots : new HashMap<>());
        params.put("history", history != null ? history : "");
        return openAIClient.call(promptTemplate, params);
    }
}
