package com.agentai.skill;

import com.agentai.common.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 计算器技能
 * 实现基本的数学运算
 */
@Component
public class CalculatorSkill implements Skill {
    
    @Override
    public String getName() {
        return "calculator";
    }
    
    @Override
    public String execute(Map<String, Object> params) {
        try {
            // 获取运算类型和操作数
            String operation = (String) params.get("operation");
            String num1Str = params.get("num1") != null ? params.get("num1").toString() : null;
            String num2Str = params.get("num2") != null ? params.get("num2").toString() : null;
            
            // 如果槽位中没有参数，尝试从原始输入中解析
            if (num1Str == null || num2Str == null) {
                String input = (String) params.get("input");
                if (input != null) {
                    Map<String, String> parsed = parseExpression(input);
                    if (operation == null) {
                        operation = parsed.get("operation");
                    }
                    if (num1Str == null) {
                        num1Str = parsed.get("num1");
                    }
                    if (num2Str == null) {
                        num2Str = parsed.get("num2");
                    }
                }
            }
            
            if (num1Str == null || num2Str == null || operation == null) {
                return "请提供完整的计算表达式，例如：计算 123 + 456";
            }
            
            Double num1 = Double.parseDouble(num1Str);
            Double num2 = Double.parseDouble(num2Str);
            
            // 执行运算
            double result;
            switch (operation) {
                case "add":
                case "+":
                    result = num1 + num2;
                    break;
                case "subtract":
                case "-":
                    result = num1 - num2;
                    break;
                case "multiply":
                case "*":
                case "×":
                    result = num1 * num2;
                    break;
                case "divide":
                case "/":
                case "÷":
                    if (num2 == 0) {
                        return "除数不能为零";
                    }
                    result = num1 / num2;
                    break;
                default:
                    return "不支持的运算类型";
            }
            
            // 构建回复
            return String.format("%.2f %s %.2f = %.2f", num1, getOperationSymbol(operation), num2, result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("计算失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析数学表达式
     * @param input 输入字符串
     * @return 解析结果
     */
    private Map<String, String> parseExpression(String input) {
        // 匹配数字和运算符
        Pattern pattern = Pattern.compile("([0-9.]+)\\s*([+\\-×*÷/])\\s*([0-9.]+)");
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.find()) {
            String num1 = matcher.group(1);
            String operator = matcher.group(2);
            String num2 = matcher.group(3);
            
            // 将运算符转换为标准格式
            String operation;
            switch (operator) {
                case "+":
                    operation = "add";
                    break;
                case "-":
                    operation = "subtract";
                    break;
                case "×":
                case "*":
                    operation = "multiply";
                    break;
                case "÷":
                case "/":
                    operation = "divide";
                    break;
                default:
                    operation = operator;
            }
            
            return Map.of("num1", num1, "operation", operation, "num2", num2);
        }
        
        return Map.of();
    }
    
    /**
     * 获取运算符号
     * @param operation 运算类型
     * @return 运算符号
     */
    private String getOperationSymbol(String operation) {
        switch (operation) {
            case "add":
                return "+";
            case "subtract":
                return "-";
            case "multiply":
                return "×";
            case "divide":
                return "÷";
            default:
                return operation;
        }
    }
    
    @Override
    public boolean canHandle(String intent) {
        return intent.contains("计算") || intent.contains("加") || intent.contains("减") || 
               intent.contains("乘") || intent.contains("除") || intent.contains("等于");
    }
}
