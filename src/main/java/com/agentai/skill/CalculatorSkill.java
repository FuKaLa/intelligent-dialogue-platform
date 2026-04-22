package com.agentai.skill;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.List;

/**
 * 计算器Skill
 * 实现数学计算功能
 */
@Component
public class CalculatorSkill {
    
    /**
     * 执行计算
     * @param slots 槽位信息
     * @return 计算结果
     */
    public String execute(Map<String, Object> slots) {
        try {
            // 获取数字和操作符
            List<String> numbers = (List<String>) slots.get("numbers");
            List<String> operations = (List<String>) slots.get("operation");
            
            if (numbers == null || numbers.size() < 2) {
                return "请提供至少两个数字进行计算";
            }
            
            if (operations == null || operations.isEmpty()) {
                return "请提供操作符进行计算";
            }
            
            // 执行计算
            double result = Double.parseDouble(numbers.get(0));
            for (int i = 0; i < operations.size() && i + 1 < numbers.size(); i++) {
                String operation = operations.get(i);
                double number = Double.parseDouble(numbers.get(i + 1));
                
                switch (operation) {
                    case "+":
                        result += number;
                        break;
                    case "-":
                        result -= number;
                        break;
                    case "*":
                        result *= number;
                        break;
                    case "/":
                        if (number == 0) {
                            return "除数不能为零";
                        }
                        result /= number;
                        break;
                    default:
                        return "不支持的操作符: " + operation;
                }
            }
            
            return "计算结果: " + result;
        } catch (Exception e) {
            e.printStackTrace();
            return "计算失败: " + e.getMessage();
        }
    }
}
