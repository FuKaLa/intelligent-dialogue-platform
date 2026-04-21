package com.agentai.dst;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 对话状态管理器
 * 使用Redis + Spring Cache管理对话上下文
 */
@Component
public class DialogueStateManager {
    
    /**
     * 获取对话状态
     */
    @Cacheable(value = "dialogueState", key = "#conversationId")
    public Map<String, Object> getState(String conversationId) {
        // 如果缓存中不存在，返回空Map
        return new HashMap<>();
    }
    
    /**
     * 更新对话状态
     */
    @CachePut(value = "dialogueState", key = "#conversationId")
    public Map<String, Object> updateState(String conversationId, Map<String, Object> state) {
        return state;
    }
    
    /**
     * 清除对话状态
     */
    @CacheEvict(value = "dialogueState", key = "#conversationId")
    public void clearState(String conversationId) {
        // 清除缓存
    }
    
    /**
     * 添加对话历史
     */
    public void addHistory(String conversationId, String userMessage, String botMessage) {
        Map<String, Object> state = getState(conversationId);
        
        // 确保历史记录存在
        if (!state.containsKey("history")) {
            state.put("history", new StringBuilder());
        }
        
        // 添加对话历史
        StringBuilder history = (StringBuilder) state.get("history");
        history.append("用户: ").append(userMessage).append("\n");
        history.append("机器人: ").append(botMessage).append("\n");
        
        // 更新状态
        updateState(conversationId, state);
    }
    
    /**
     * 获取对话历史
     */
    public String getHistory(String conversationId) {
        Map<String, Object> state = getState(conversationId);
        if (state.containsKey("history")) {
            return state.get("history").toString();
        }
        return "";
    }
}