package com.agentai.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 向量生成服务
 * 用于将文本转换为向量表示，支持真实的向量模型调用
 * 
 * <p>主要功能：
 * 1. 调用 bge_m3 模型生成文本向量
 * 2. 支持单个文本和批量文本的向量生成
 * 3. 集成缓存机制，避免重复计算
 * 4. 提供错误处理和异常捕获
 * </p>
 * 
 * <p>使用场景：
 * - 语义检索：将查询文本和文档转换为向量，计算相似度
 * - 文本分类：基于向量进行文本分类
 * - 聚类分析：基于向量相似度进行文本聚类
 * </p>
 */
@Service
public class EmbeddingService {
    
    /**
     * RestTemplate 用于调用向量模型 API
     */
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 缓存服务，用于缓存向量生成结果
     */
    @Autowired
    private CacheService cacheService;
    
    /**
     * 向量模型 API 基础 URL
     */
    @Value("${middleware.embedding.base-url}")
    private String embeddingBaseUrl;
    
    /**
     * 向量模型 API 密钥
     */
    @Value("${middleware.embedding.api-key}")
    private String apiKey;
    
    /**
     * 使用的向量模型名称
     */
    @Value("${middleware.embedding.model}")
    private String model;
    
    /**
     * bge_m3 模型的向量维度
     */
    private static final int DIMENSION = 768; // bge_m3 模型的向量维度
    
    /**
     * 生成单个文本的向量表示
     * 
     * @param text 输入文本
     * @return 文本的向量表示，长度为 DIMENSION
     * @throws BusinessException 向量生成失败时抛出异常
     */
    public List<Float> generateEmbedding(String text) {
        try {
            // 构建 API 请求 URL
            String url = embeddingBaseUrl + "/embeddings";
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("input", text);
            
            // 创建 HTTP 请求实体
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送 POST 请求并获取响应
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            // 处理响应结果
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                List<Double> embedding = (List<Double>) data.get(0).get("embedding");
                
                // 将 Double 类型的向量转换为 Float 类型
                List<Float> floatEmbedding = new ArrayList<>();
                for (Double value : embedding) {
                    floatEmbedding.add(value.floatValue());
                }
                
                return floatEmbedding;
            } else {
                // 响应失败时抛出异常
                throw new BusinessException("向量生成失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // 捕获所有异常并转换为业务异常
            e.printStackTrace();
            throw new BusinessException("向量生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成文本向量（带缓存）
     * 
     * @param text 输入文本
     * @return 文本的向量表示
     * <p>使用缓存的好处：
     * 1. 减少 API 调用次数，降低延迟
     * 2. 节省计算资源和 API 费用
     * 3. 提高系统响应速度
     * </p>
     */
    public List<Float> generateEmbeddingWithCache(String text) {
        try {
            // 尝试从缓存获取向量
            List<Float> cached = cacheService.getCachedEmbedding(text);
            if (cached != null) {
                // 缓存命中，直接返回
                return cached;
            }
            
            // 缓存未命中，生成新向量
            List<Float> embedding = generateEmbedding(text);
            // 将新生成的向量存入缓存
            cacheService.cacheEmbedding(text, embedding);
            return embedding;
        } catch (Exception e) {
            // 缓存操作失败时，直接生成向量
            e.printStackTrace();
            return generateEmbedding(text);
        }
    }
    
    /**
     * 批量生成文本向量
     * 
     * @param texts 输入文本列表
     * @return 文本向量列表，每个元素对应输入文本的向量
     * <p>批量生成的优势：
     * 1. 减少 HTTP 请求次数
     * 2. 提高处理效率
     * 3. 适用于批量文档处理场景
     * </p>
     */
    public List<List<Float>> generateEmbeddings(List<String> texts) {
        try {
            // 构建 API 请求 URL
            String url = embeddingBaseUrl + "/embeddings";
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("input", texts);
            
            // 创建 HTTP 请求实体
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送 POST 请求并获取响应
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            // 处理响应结果
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                
                // 处理每个文本的向量
                List<List<Float>> embeddings = new ArrayList<>();
                for (Map<String, Object> item : data) {
                    List<Double> embedding = (List<Double>) item.get("embedding");
                    
                    // 将 Double 类型的向量转换为 Float 类型
                    List<Float> floatEmbedding = new ArrayList<>();
                    for (Double value : embedding) {
                        floatEmbedding.add(value.floatValue());
                    }
                    
                    embeddings.add(floatEmbedding);
                }
                
                return embeddings;
            } else {
                // 响应失败时抛出异常
                throw new BusinessException("向量生成失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // 捕获所有异常并转换为业务异常
            e.printStackTrace();
            throw new BusinessException("向量生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取向量维度
     * 
     * @return 向量维度
     */
    public int getDimension() {
        return DIMENSION;
    }
}
