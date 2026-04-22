package com.agentai.skill;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Skill管理器
 * 管理和执行各种Skill
 */
@Component
public class SkillManager {
    
    @Autowired
    private CalculatorSkill calculatorSkill;
    
    @Autowired
    private WeatherSkill weatherSkill;
    
    @Autowired
    private SearchSkill searchSkill;
    
    @Autowired
    private GreetingSkill greetingSkill;
    
    @Autowired
    private HelpSkill helpSkill;
    
    @Autowired
    private ChitchatSkill chitchatSkill;
    
    /**
     * 执行Skill
     * @param skillName Skill名称
     * @param slots 槽位信息
     * @return Skill执行结果
     */
    public String executeSkill(String skillName, Map<String, Object> slots) {
        switch (skillName) {
            case "calculator":
                return calculatorSkill.execute(slots);
            case "weather":
                return weatherSkill.execute(slots);
            case "search":
                return searchSkill.execute(slots);
            case "greeting":
                return greetingSkill.execute(slots);
            case "help":
                return helpSkill.execute(slots);
            case "chitchat":
                return chitchatSkill.execute(slots);
            default:
                return "未知技能";
        }
    }
}
