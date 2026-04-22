package com.agentai.nlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本归一化器
 * 实现文本清洗、分词、关键词提取等功能
 */
@Component
public class TextNormalizer {
    
    // 表情符号正则
    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+");
    
    // 停用词列表
    private static final List<String> STOP_WORDS = List.of(
            "的", "了", "是", "在", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这"
    );
    
    /**
     * 文本归一化
     * @param text 原始文本
     * @return 归一化后的文本
     */
    public String normalize(String text) {
        if (text == null) {
            return "";
        }
        
        // 1. 去除首尾空格
        text = text.trim();
        
        // 2. 全角转半角
        text = fullToHalfWidth(text);
        
        // 3. 过滤表情符号
        text = EMOJI_PATTERN.matcher(text).replaceAll("");
        
        // 4. 统一标点符号
        text = text.replaceAll("[，]", ",")
                  .replaceAll("[。]", ".")
                  .replaceAll("[！]", "!")
                  .replaceAll("[？]", "?")
                  .replaceAll("[；]", ";")
                  .replaceAll("[：]", ":")
                  .replaceAll("[（]", "(")
                  .replaceAll("[）]", ")")
                  .replaceAll("[【]", "[")
                  .replaceAll("[】]", "]");
        
        // 5. 转简体
        text = HanLP.convertToSimplifiedChinese(text);
        
        return text;
    }
    
    /**
     * 全角转半角
     * @param text 全角文本
     * @return 半角文本
     */
    private String fullToHalfWidth(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == 12288) { // 全角空格
                chars[i] = ' ';
            } else if (chars[i] >= 65281 && chars[i] <= 65374) { // 全角字符
                chars[i] = (char) (chars[i] - 65248);
            }
        }
        return new String(chars);
    }
    
    /**
     * 分词
     * @param text 文本
     * @return 分词结果
     */
    public List<Term> segment(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        return NLPTokenizer.segment(text);
    }
    
    /**
     * 分词并过滤停用词
     * @param text 文本
     * @return 过滤后的分词结果
     */
    public List<Term> segmentWithFilter(String text) {
        List<Term> terms = segment(text);
        List<Term> filteredTerms = new ArrayList<>();
        for (Term term : terms) {
            if (!STOP_WORDS.contains(term.word)) {
                filteredTerms.add(term);
            }
        }
        return filteredTerms;
    }
    
    /**
     * 提取关键词
     * @param text 文本
     * @return 关键词列表
     */
    public List<String> extractKeywords(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        // 使用HanLP提取关键词
        List<String> keywords = HanLP.extractKeyword(text, 10);
        // 过滤停用词
        List<String> filteredKeywords = new ArrayList<>();
        for (String keyword : keywords) {
            if (!STOP_WORDS.contains(keyword)) {
                filteredKeywords.add(keyword);
            }
        }
        return filteredKeywords;
    }
    
    /**
     * 提取关键词（基于分词结果）
     * @param terms 分词结果
     * @return 关键词列表
     */
    public List<String> extractKeywords(List<Term> terms) {
        List<String> keywords = new ArrayList<>();
        for (Term term : terms) {
            // 过滤停用词，只保留名词、动词、形容词
            String nature = term.nature.toString();
            if (!STOP_WORDS.contains(term.word) && 
                (nature.startsWith("n") || nature.startsWith("v") || nature.startsWith("a"))) {
                keywords.add(term.word);
            }
        }
        return keywords;
    }
    
    /**
     * 转换为拼音
     * @param text 文本
     * @return 拼音
     */
    public String convertToPinyin(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return HanLP.convertToPinyinString(text, " ", true);
    }
}
