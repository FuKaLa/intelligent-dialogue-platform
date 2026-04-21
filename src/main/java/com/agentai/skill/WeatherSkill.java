package com.agentai.skill;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 天气查询技能
 */
@Component
public class WeatherSkill implements Skill {
    
    @Override
    public String getName() {
        return "weather";
    }
    
    @Override
    public String execute(Map<String, Object> params) {
        // 模拟天气查询
        String city = (String) params.get("city");
        if (city == null) {
            return "请提供城市名称";
        }
        return "当前" + city + "的天气晴朗，温度25℃";
    }
    
    @Override
    public boolean canHandle(String intent) {
        return intent.contains("天气") || intent.contains("温度");
    }
}