package com.agentai.nlp;

import com.agentai.common.BusinessException;
import com.agentai.common.OpenAIClient;
import com.hankcs.hanlp.seg.common.Term;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 实体提取器
 * 实现规则/词典/分词匹配 → LLM补全的混合架构
 */
@Component
public class EntityExtractor {
    
    @Autowired
    private OpenAIClient openAIClient;
    
    @Autowired
    private TextNormalizer textNormalizer;
    
    // 实体类型定义
    public static final String ENTITY_TYPE_CITY = "city";
    public static final String ENTITY_TYPE_DATE = "date";
    public static final String ENTITY_TYPE_NUMBER = "number";
    public static final String ENTITY_TYPE_OPERATION = "operation";
    public static final String ENTITY_TYPE_KEYWORD = "keyword";
    
    // 城市名称列表
    public static final List<String> CITY_LIST = Arrays.asList(
            "北京", "上海", "广州", "深圳", "杭州", "南京", "武汉", "成都", "重庆", "西安",
            "长沙", "福州", "厦门", "青岛", "大连", "天津", "苏州", "宁波", "无锡", "济南"
    );
    
    // 日期正则
    private static final Pattern DATE_PATTERN = Pattern.compile("(今天|明天|后天|大后天|昨天|前天|大前天|本周|下周|上周|本月|下月|上月|\\d{4}年\\d{1,2}月\\d{1,2}日|\\d{1,2}月\\d{1,2}日|\\d{4}-\\d{1,2}-\\d{1,2}|\\d{1,2}/\\d{1,2}/\\d{4})");
    
    // 数字正则
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+(\\.\\d+)?");
    
    // 操作符正则
    private static final Pattern OPERATION_PATTERN = Pattern.compile("[+\\-*/]");
    
    /**
     * 提取实体
     * @param text 用户输入文本
     * @return 提取的实体
     */
    public Map<String, Object> extract(String text) {
        return extractWithContext(text, "");
    }
    
    /**
     * 提取实体（带上下文）
     * @param text 用户输入文本
     * @param context 对话上下文
     * @return 提取的实体
     */
    public Map<String, Object> extractWithContext(String text, String context) {
        // 1. 文本归一化
        String normalizedText = textNormalizer.normalize(text);
        
        // 2. HanLP分词
        List<Term> terms = textNormalizer.segmentWithFilter(normalizedText);
        
        // 3. 提取关键词
        List<String> keywords = textNormalizer.extractKeywords(terms);
        
        // 4. 规则实体抽取
        Map<String, Object> ruleEntities = extractRuleEntities(normalizedText, terms, keywords);
        
        // 5. LLM实体补全
        Map<String, Object> llmEntities = extractLLMEntities(normalizedText, context, ruleEntities);
        
        // 6. 合并实体
        Map<String, Object> combinedEntities = new HashMap<>();
        combinedEntities.putAll(ruleEntities);
        combinedEntities.putAll(llmEntities);
        
        return combinedEntities;
    }
    
    /**
     * 规则实体抽取
     * @param text 归一化文本
     * @param terms 分词结果
     * @param keywords 关键词列表
     * @return 提取的实体
     */
    private Map<String, Object> extractRuleEntities(String text, List<Term> terms, List<String> keywords) {
        Map<String, Object> entities = new HashMap<>();
        
        // 提取城市
        List<String> cities = extractCities(text, terms);
        if (!cities.isEmpty()) {
            entities.put(ENTITY_TYPE_CITY, cities);
        }
        
        // 提取日期
        List<String> dates = extractDates(text);
        if (!dates.isEmpty()) {
            entities.put(ENTITY_TYPE_DATE, dates);
        }
        
        // 提取数字
        List<String> numbers = extractNumbers(text);
        if (!numbers.isEmpty()) {
            entities.put(ENTITY_TYPE_NUMBER, numbers);
        }
        
        // 提取操作符
        List<String> operations = extractOperations(text);
        if (!operations.isEmpty()) {
            entities.put(ENTITY_TYPE_OPERATION, operations);
        }
        
        // 提取关键词
        if (!keywords.isEmpty()) {
            entities.put(ENTITY_TYPE_KEYWORD, keywords);
        }
        
        return entities;
    }
    
    /**
     * 提取城市
     * @param text 文本
     * @param terms 分词结果
     * @return 城市列表
     */
    private List<String> extractCities(String text, List<Term> terms) {
        List<String> cities = new ArrayList<>();
        
        // 1. 从文本中提取城市
        for (String city : CITY_LIST) {
            if (text.contains(city)) {
                cities.add(city);
            }
        }
        
        // 2. 从分词结果中提取城市
        for (Term term : terms) {
            if (CITY_LIST.contains(term.word)) {
                if (!cities.contains(term.word)) {
                    cities.add(term.word);
                }
            }
        }
        
        return cities;
    }
    
    /**
     * 提取日期
     * @param text 文本
     * @return 日期列表
     */
    private List<String> extractDates(String text) {
        List<String> dates = new ArrayList<>();
        Matcher matcher = DATE_PATTERN.matcher(text);
        while (matcher.find()) {
            dates.add(matcher.group());
        }
        return dates;
    }
    
    /**
     * 提取数字
     * @param text 文本
     * @return 数字列表
     */
    private List<String> extractNumbers(String text) {
        List<String> numbers = new ArrayList<>();
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }
    
    /**
     * 提取操作符
     * @param text 文本
     * @return 操作符列表
     */
    private List<String> extractOperations(String text) {
        List<String> operations = new ArrayList<>();
        Matcher matcher = OPERATION_PATTERN.matcher(text);
        while (matcher.find()) {
            operations.add(matcher.group());
        }
        return operations;
    }
    
    /**
     * 使用LLM提取实体
     * @param text 归一化文本
     * @param context 对话上下文
     * @param ruleEntities 规则提取的实体
     * @return LLM提取的实体
     */
    private Map<String, Object> extractLLMEntities(String text, String context, Map<String, Object> ruleEntities) {
        String promptTemplate = "请根据以下对话上下文和用户输入，提取实体信息。\n" +
                "上下文：{context}\n" +
                "用户输入：{text}\n" +
                "已提取的实体：{ruleEntities}\n" +
                "请以JSON格式返回提取的实体，包含以下可能的实体类型：city（城市）、date（日期）、number（数字）、operation（操作符）、keyword（关键词）。\n" +
                "例如：{\"city\": [\"北京\"], \"date\": [\"今天\"], \"number\": [\"10\", \"20\"], \"operation\": [\"+\"], \"keyword\": [\"天气\"]}\n" +
                "请只返回JSON格式的实体信息，不要返回其他任何内容。";
        
        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("text", text);
        params.put("ruleEntities", ruleEntities);
        
        try {
            String result = openAIClient.call(promptTemplate, params);
            return parseLLMResponse(result);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("实体提取失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析LLM返回的响应
     * @param response LLM返回的响应
     * @return 解析后的实体
     */
    private Map<String, Object> parseLLMResponse(String response) {
        // 简单的JSON解析，实际项目中可以使用JSON库
        Map<String, Object> entities = new HashMap<>();
        
        // 去除多余的文字
        response = response.trim();
        if (response.startsWith("{") && response.endsWith("}")) {
            try {
                // 这里使用简化的解析，实际项目中应该使用JSON库
                // 提取city
                if (response.contains("\"city\":")) {
                    int cityStart = response.indexOf("\"city\":") + 7;
                    int cityEnd = response.indexOf("]", cityStart);
                    if (cityEnd > cityStart) {
                        String cityStr = response.substring(cityStart + 1, cityEnd);
                        List<String> cities = Arrays.asList(cityStr.split(","));
                        List<String> trimmedCities = new ArrayList<>();
                        for (String city : cities) {
                            trimmedCities.add(city.trim().replaceAll("\"", ""));
                        }
                        entities.put(ENTITY_TYPE_CITY, trimmedCities);
                    }
                }
                
                // 提取date
                if (response.contains("\"date\":")) {
                    int dateStart = response.indexOf("\"date\":") + 7;
                    int dateEnd = response.indexOf("]", dateStart);
                    if (dateEnd > dateStart) {
                        String dateStr = response.substring(dateStart + 1, dateEnd);
                        List<String> dates = Arrays.asList(dateStr.split(","));
                        List<String> trimmedDates = new ArrayList<>();
                        for (String date : dates) {
                            trimmedDates.add(date.trim().replaceAll("\"", ""));
                        }
                        entities.put(ENTITY_TYPE_DATE, trimmedDates);
                    }
                }
                
                // 提取number
                if (response.contains("\"number\":")) {
                    int numberStart = response.indexOf("\"number\":") + 9;
                    int numberEnd = response.indexOf("]", numberStart);
                    if (numberEnd > numberStart) {
                        String numberStr = response.substring(numberStart + 1, numberEnd);
                        List<String> numbers = Arrays.asList(numberStr.split(","));
                        List<String> trimmedNumbers = new ArrayList<>();
                        for (String number : numbers) {
                            trimmedNumbers.add(number.trim().replaceAll("\"", ""));
                        }
                        entities.put(ENTITY_TYPE_NUMBER, trimmedNumbers);
                    }
                }
                
                // 提取operation
                if (response.contains("\"operation\":")) {
                    int operationStart = response.indexOf("\"operation\":") + 13;
                    int operationEnd = response.indexOf("]", operationStart);
                    if (operationEnd > operationStart) {
                        String operationStr = response.substring(operationStart + 1, operationEnd);
                        List<String> operations = Arrays.asList(operationStr.split(","));
                        List<String> trimmedOperations = new ArrayList<>();
                        for (String operation : operations) {
                            trimmedOperations.add(operation.trim().replaceAll("\"", ""));
                        }
                        entities.put(ENTITY_TYPE_OPERATION, trimmedOperations);
                    }
                }
                
                // 提取keyword
                if (response.contains("\"keyword\":")) {
                    int keywordStart = response.indexOf("\"keyword\":") + 10;
                    int keywordEnd = response.indexOf("]", keywordStart);
                    if (keywordEnd > keywordStart) {
                        String keywordStr = response.substring(keywordStart + 1, keywordEnd);
                        List<String> keywords = Arrays.asList(keywordStr.split(","));
                        List<String> trimmedKeywords = new ArrayList<>();
                        for (String keyword : keywords) {
                            trimmedKeywords.add(keyword.trim().replaceAll("\"", ""));
                        }
                        entities.put(ENTITY_TYPE_KEYWORD, trimmedKeywords);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return entities;
            }
        }
        
        return entities;
    }
}
