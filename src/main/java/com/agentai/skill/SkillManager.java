package com.agentai.skill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 技能管理器
 */
@Component
public class SkillManager {
    
    @Autowired
    private List<Skill> skills;
    
    /**
     * 执行技能
     */
    public String executeSkill(String intent, Map<String, Object> params) {
        // 找到可以处理该意图的技能
        for (Skill skill : skills) {
            if (skill.canHandle(intent)) {
                return skill.execute(params);
            }
        }
        return "抱歉，我无法处理这个请求";
    }
    
    /**
     * 获取所有技能
     */
    public List<Skill> getSkills() {
        return skills;
    }
}