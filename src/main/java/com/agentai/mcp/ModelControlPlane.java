package com.agentai.mcp;

import com.agentai.dst.DialogueStateManager;
import com.agentai.nlp.NLUService;
import com.agentai.search.SemanticSearchService;
import com.agentai.skill.SkillManager;
import com.agentai.nlg.NLGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 模型控制平面
 * 实现意图路由、Skill选择、流程编排逻辑
 */
@Component
public class ModelControlPlane {
    
    @Autowired
    private NLUService nluService;
    
    @Autowired
    private DialogueStateManager dialogueStateManager;
    
    @Autowired
    private SemanticSearchService semanticSearchService;
    
    @Autowired
    private SkillManager skillManager;
    
    @Autowired
    private NLGService nlgService;
    
    /**
     * 处理对话请求
     * @param conversationId 对话ID
     * @param userInput 用户输入
     * @return 处理结果
     */
    public Map<String, Object> processDialogue(String conversationId, String userInput) {
        // 1. 执行NLU处理
        Map<String, Object> nluResult = nluService.processWithContext(userInput, conversationId);
        
        // 2. 执行语义检索
        List<Map<String, Object>> searchResults = semanticSearchService.search(userInput, 5);
        
        // 3. 选择并执行Skill
        String skillResult = executeSkill(nluResult, searchResults, conversationId);
        
        // 4. 更新对话状态
        dialogueStateManager.updateState(conversationId, nluResult, skillResult);
        
        // 5. 生成回复
        String response = nlgService.generateResponse(nluResult, skillResult, searchResults, conversationId);
        
        // 6. 构建返回结果
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("response", response);
        result.put("intent", nluResult.get("intent"));
        result.put("entities", nluResult.get("entities"));
        result.put("slots", nluResult.get("slots"));
        result.put("searchResults", searchResults);
        
        return result;
    }
    
    /**
     * 选择并执行Skill
     * @param nluResult NLU结果
     * @param searchResults 检索结果
     * @param conversationId 对话ID
     * @return Skill执行结果
     */
    private String executeSkill(Map<String, Object> nluResult, List<Map<String, Object>> searchResults, String conversationId) {
        String intent = (String) nluResult.get("intent");
        Map<String, Object> slots = (Map<String, Object>) nluResult.get("slots");
        
        // 根据意图选择Skill
        switch (intent) {
            case "calculator":
                return skillManager.executeSkill("calculator", slots);
            case "weather":
                return skillManager.executeSkill("weather", slots);
            case "search":
                return skillManager.executeSkill("search", slots);
            case "greeting":
                return skillManager.executeSkill("greeting", slots);
            case "help":
                return skillManager.executeSkill("help", slots);
            default:
                return skillManager.executeSkill("chitchat", slots);
        }
    }
}
