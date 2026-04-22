package com.agentai.dst;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 对话状态管理器
 * 实现对话上下文、槽位状态、历史意图、指代记录的真实读写
 */
@Component
public class DialogueStateManager {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String DIALOGUE_STATE_PREFIX = "dialogue:state:";
    private static final long SESSION_EXPIRATION_TIME = 24 * 60 * 60; // 24小时
    
    /**
     * 获取对话状态
     * @param conversationId 对话ID
     * @return 对话状态
     */
    public Map<String, Object> getState(String conversationId) {
        String key = DIALOGUE_STATE_PREFIX + conversationId;
        Map<String, Object> state = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        if (state == null) {
            state = new HashMap<>();
            state.put("history", "");
            state.put("slots", new HashMap<String, Object>());
            state.put("intent", "");
            state.put("entities", new HashMap<String, Object>());
            state.put("skillResult", null);
            saveState(conversationId, state);
        }
        return state;
    }
    
    /**
     * 保存对话状态
     * @param conversationId 对话ID
     * @param state 对话状态
     */
    public void saveState(String conversationId, Map<String, Object> state) {
        String key = DIALOGUE_STATE_PREFIX + conversationId;
        redisTemplate.opsForValue().set(key, state, SESSION_EXPIRATION_TIME, TimeUnit.SECONDS);
    }
    
    /**
     * 更新对话状态
     * @param conversationId 对话ID
     * @param nluResult NLU结果
     * @param skillResult Skill执行结果
     */
    public void updateState(String conversationId, Map<String, Object> nluResult, String skillResult) {
        Map<String, Object> state = getState(conversationId);
        
        // 更新历史
        String history = (String) state.get("history");
        String newHistory = history + "\n" + nluResult.get("intent") + ": " + nluResult.get("entities");
        state.put("history", newHistory);
        
        // 更新意图
        state.put("intent", nluResult.get("intent"));
        
        // 更新实体
        state.put("entities", nluResult.get("entities"));
        
        // 更新槽位
        state.put("slots", nluResult.get("slots"));
        
        // 更新Skill执行结果
        state.put("skillResult", skillResult);
        
        // 保存更新后的状态
        saveState(conversationId, state);
    }
    
    /**
     * 获取对话历史
     * @param conversationId 对话ID
     * @return 对话历史
     */
    public String getHistory(String conversationId) {
        Map<String, Object> state = getState(conversationId);
        return (String) state.get("history");
    }
    
    /**
     * 获取槽位状态
     * @param conversationId 对话ID
     * @return 槽位状态
     */
    public Map<String, Object> getSlots(String conversationId) {
        Map<String, Object> state = getState(conversationId);
        return (Map<String, Object>) state.get("slots");
    }
    
    /**
     * 获取历史意图
     * @param conversationId 对话ID
     * @return 历史意图
     */
    public String getIntent(String conversationId) {
        Map<String, Object> state = getState(conversationId);
        return (String) state.get("intent");
    }
    
    /**
     * 获取历史实体
     * @param conversationId 对话ID
     * @return 历史实体
     */
    public Map<String, Object> getEntities(String conversationId) {
        Map<String, Object> state = getState(conversationId);
        return (Map<String, Object>) state.get("entities");
    }
    
    /**
     * 获取Skill执行结果
     * @param conversationId 对话ID
     * @return Skill执行结果
     */
    public String getSkillResult(String conversationId) {
        Map<String, Object> state = getState(conversationId);
        return (String) state.get("skillResult");
    }
    
    /**
     * 删除对话状态
     * @param conversationId 对话ID
     */
    public void deleteState(String conversationId) {
        String key = DIALOGUE_STATE_PREFIX + conversationId;
        redisTemplate.delete(key);
    }
    
    /**
     * 检查对话状态是否存在
     * @param conversationId 对话ID
     * @return 是否存在
     */
    public boolean exists(String conversationId) {
        String key = DIALOGUE_STATE_PREFIX + conversationId;
        return redisTemplate.hasKey(key);
    }
}
