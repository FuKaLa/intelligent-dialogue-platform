package com.agentai.skill;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;

/**
 * 天气查询Skill
 * 实现天气查询功能
 */
@Component
public class WeatherSkill {
    
    /**
     * 执行天气查询
     * @param slots 槽位信息
     * @return 天气查询结果
     */
    public String execute(Map<String, Object> slots) {
        try {
            // 获取城市和日期
            List<String> cities = (List<String>) slots.get("city");
            List<String> dates = (List<String>) slots.get("date");
            
            String city = cities != null && !cities.isEmpty() ? cities.get(0) : "北京";
            String date = dates != null && !dates.isEmpty() ? dates.get(0) : "今天";
            
            // 模拟天气数据，实际项目中应该调用天气API
            // 这里返回模拟数据，实际项目中应该调用真实的天气API
            return date + city + "的天气：晴，温度 25-30℃，风力 3级，空气质量良";
        } catch (Exception e) {
            e.printStackTrace();
            return "天气查询失败: " + e.getMessage();
        }
    }
}
