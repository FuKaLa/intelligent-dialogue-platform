package com.agentai.nlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.seg.common.Term;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文本归一化处理
 */
@Component
public class TextNormalizer {
    
    /**
     * 对文本进行归一化处理
     * @param text 原始文本
     * @return 归一化后的文本
     */
    public String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 1. 去除首尾空格
        text = text.trim();
        
        // 2. 统一标点符号
        text = text.replaceAll("[，]", ",")
                  .replaceAll("[。]", ".")
                  .replaceAll("[！]", "!")
                  .replaceAll("[？]", "?")
                  .replaceAll("[；]", ";")
                  .replaceAll("[：]", ":")
                  .replaceAll("[“”]", "\"")
                  .replaceAll("[‘’]", "'")
                  .replaceAll("[（]", "(")
                  .replaceAll("[）]", ")")
                  .replaceAll("[【]", "[")
                  .replaceAll("[】]", "]");
        
        // 3. 简繁转换（转为简体）
        text = HanLP.convertToSimplifiedChinese(text);
        
        // 4. 去除特殊字符
        text = text.replaceAll("[\\p{Cntrl}]", "");
        
        // 5. 去除多余空格
        text = text.replaceAll("\\s+", " ");
        
        return text;
    }
    
    /**
     * 对文本进行分词
     * @param text 原始文本
     * @return 分词结果
     */
    public List<Term> segment(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return HanLP.segment(text);
    }
    
    /**
     * 提取文本关键词
     * @param text 原始文本
     * @param topN 提取数量
     * @return 关键词列表
     */
    public List<String> extractKeywords(String text, int topN) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return HanLP.extractKeyword(text, topN);
    }
    
    /**
     * 获取文本拼音
     * @param text 原始文本
     * @return 拼音列表
     */
    public List<Pinyin> convertToPinyin(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return HanLP.convertToPinyinList(text);
    }
}