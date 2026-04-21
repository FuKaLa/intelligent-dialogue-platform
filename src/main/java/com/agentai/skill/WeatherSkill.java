package com.agentai.skill;

import com.agentai.common.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 天气查询技能
 * 使用真实的天气API获取天气信息
 */
@Component
public class WeatherSkill implements Skill {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "your_api_key"; // 请替换为真实的API密钥
    
    @Override
    public String getName() {
        return "weather";
    }
    
    @Override
    public String execute(Map<String, Object> params) {
        // 获取城市名称，支持多种槽位格式
        String city = null;
        if (params.containsKey("city")) {
            city = (String) params.get("city");
        } else if (params.containsKey("location")) {
            city = (String) params.get("location");
        } else if (params.containsKey("地名")) {
            city = (String) params.get("地名");
        }
        
        if (city == null || city.isEmpty()) {
            return "请提供城市名称";
        }
        
        try {
            // 构建API请求URL
            String url = WEATHER_API_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric&lang=zh_cn";
            
            // 调用天气API
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                // 解析天气数据
                Map<String, Object> main = (Map<String, Object>) response.get("main");
                double temp = (double) main.get("temp");
                int humidity = (int) main.get("humidity");
                
                Map<String, Object> weather = ((java.util.List<Map<String, Object>>) response.get("weather")).get(0);
                String description = (String) weather.get("description");
                
                // 构建回复
                return String.format("当前%s的天气%s，温度%.1f℃，湿度%d%%", 
                        city, description, temp, humidity);
            } else {
                return "无法获取天气信息，请稍后重试";
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("天气查询失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean canHandle(String intent) {
        return intent.contains("天气") || intent.contains("温度") || intent.contains("湿度");
    }
}
