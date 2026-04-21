package com.agentai.skill;

import java.util.Map;

/**
 * 技能接口
 */
public interface Skill {
    
    /**
     * 获取技能名称
     */
    String getName();
    
    /**
     * 执行技能
     */
    String execute(Map<String, Object> params);
    
    /**
     * 检查是否可以执行该技能
     */
    boolean canHandle(String intent);
}