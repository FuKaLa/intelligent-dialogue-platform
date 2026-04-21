package com.agentai.dst;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 对话状态管理器
 * 使用Redis真实存储对话上下文
 */
@Component
public class DialogueStateManager {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String DIALOGUE_STATE_PREFIX = "agentai:dialogue:state:";
    private static final long STATE_EXPIRATION_HOURS = 24; // 对话状态过期时间（小时）
    
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
            // 初始化对话状态
            state.put("history", "");
            state.put("slots", new HashMap<String, Object>());
            state.put("intent", "");
            state.put("lastUpdated", System.currentTimeMillis());
            // 保存到Redis
            saveState(conversationId, state);
        }
        return state;
    }
    
    /**
     * 更新对话状态
     * @param conversationId 对话ID
     * @param state 对话状态
     * @return 更新后的对话状态
     */
    public Map<String, Object> updateState(String conversationId, Map<String, Object> state) {
        // 更新最后更新时间
        state.put("lastUpdated", System.currentTimeMillis());
        // 保存到Redis
        saveState(conversationId, state);
        return state;
    }
    
    /**
     * 保存对话状态到Redis
     * @param conversationId 对话ID
     * @param state 对话状态
     */
    private void saveState(String conversationId, Map<String, Object> state) {
        String key = DIALOGUE_STATE_PREFIX + conversationId;
        redisTemplate.opsForValue().set(key, state, STATE_EXPIRATION_HOURS, TimeUnit.HOURS);
    }
    
    /**
     * 清除对话状态
     * @param conversationId 对话ID
     */
    public void clearState(String conversationId) {
        String key = DIALOGUE_STATE_PREFIX + conversationId;
        redisTemplate.delete(key);
    }
    
    /**
     * 添加对话历史
     * @param conversationId 对话ID
     * @param userMessage 用户消息
     * @param botMessage 机器人消息
     */
    public void addHistory(String conversationId, String userMessage, String botMessage) {
        Map<String, Object> state = getState(conversationId);
        
        // 获取现有历史
        String history = (String) state.get("history");
        if (history == null) {
            history = "";
        }
        
        // 添加新的对话历史
        StringBuilder historyBuilder = new StringBuilder(history);
        historyBuilder.append("用户: " + userMessage).append("\n");
        historyBuilder.append("机器人: " + botMessage).append("\n");
        
        // 更新状态
        state.put("history", historyBuilder.toString());
        updateState(conversationId, state);
    }
    
    /**
     * 获取对话历史
     * @param conversationId 对话ID
     * @return 对话历史
     */
    public String getHistory(String conversationId) {
        Map<String, Object> state = getState(conversationId);
        String history = (String) state.get("history");
        return history != null ? history : "";
    }
    
    /**
     * 更新对话槽位
     * @param conversationId 对话ID
     * @param slots 槽位信息
     */
    public void updateSlots(String conversationId, Map<String, Object> slots) {
        Map<String, Object> state = getState(conversationId);
        Map<String, Object> existingSlots = (Map<String, Object>) state.get("slots");
        if (existingSlots == null) {
            existingSlots = new HashMap<>();
        }
        existingSlots.putAll(slots);
        state.put("slots", existingSlots);
        updateState(conversationId, state);
    }
    
    /**
     * 获取对话槽位
     * @param conversationId 对话ID
     * @return 槽位信息
     */
    public Map<String, Object> getSlots(String conversationId) {
        Map<String, Object> state = getState(conversationId);
        Map<String, Object> slots = (Map<String, Object>) state.get("slots");
        return slots != null ? slots : new HashMap<>();
    }
    
    /**
     * 更新对话意图
     * @param conversationId 对话ID
     * @param intent 意图
     */
    public void updateIntent(String conversationId, String intent) {
        Map<String, Object> state = getState(conversationId);
        state.put("intent", intent);
        updateState(conversationId, state);
    }
    
    /**
     * 获取对话意图
     * @param conversationId 对话ID
     * @return 意图
     */
    public String getIntent(String conversationId) {
        Map<String, Object> state = getState(conversationId);
        String intent = (String) state.get("intent");
        return intent != null ? intent : "";
    }
}