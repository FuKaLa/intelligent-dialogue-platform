package com.agentai.common;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 客户端工具类
 * 用于文档的存储和检索
 */
@Component
public class ElasticsearchClient {
    
    private final RestClient restClient;
    private static final String INDEX_NAME = "dialogue_documents";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 构造函数
     * @param restClient Elasticsearch REST 客户端实例
     */
    public ElasticsearchClient(RestClient restClient) {
        this.restClient = restClient;
    }
    
    /**
     * 初始化 Elasticsearch 索引
     */
    @PostConstruct
    public void init() {
        try {
            // 检查索引是否存在
            Request request = new Request("HEAD", "/" + INDEX_NAME);
            Response response = restClient.performRequest(request);
            
            if (response.getStatusLine().getStatusCode() != 200) {
                // 创建索引
                String mapping = "{\"mappings\": {\"properties\": {\"content\": {\"type\": \"text\"}, \"type\": {\"type\": \"keyword\"}, \"timestamp\": {\"type\": \"date\"}, \"metadata\": {\"type\": \"object\"}}}}";
                
                HttpEntity entity = new NStringEntity(mapping, ContentType.APPLICATION_JSON);
                request = new Request("PUT", "/" + INDEX_NAME);
                request.setEntity(entity);
                restClient.performRequest(request);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("Elasticsearch 初始化失败: " + e.getMessage());
        }
    }
    
    /**
     * 插入文档
     * @param id 文档ID
     * @param document 文档内容
     */
    public void insertDocument(String id, Map<String, Object> document) {
        try {
            String json = objectMapper.writeValueAsString(document);
            HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
            Request request = new Request("PUT", "/" + INDEX_NAME + "/_doc/" + id);
            request.setEntity(entity);
            restClient.performRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("文档插入失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文档
     * @param id 文档ID
     * @return 文档内容
     */
    public Map<String, Object> getDocument(String id) {
        try {
            Request request = new Request("GET", "/" + INDEX_NAME + "/_doc/" + id);
            Response response = restClient.performRequest(request);
            
            if (response.getStatusLine().getStatusCode() == 200) {
                InputStream inputStream = response.getEntity().getContent();
                Map<String, Object> result = objectMapper.readValue(inputStream, Map.class);
                return (Map<String, Object>) result.get("_source");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("文档获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新文档
     * @param id 文档ID
     * @param document 文档内容
     */
    public void updateDocument(String id, Map<String, Object> document) {
        try {
            java.util.HashMap<String, Object> updateRequest = new java.util.HashMap<>();
            updateRequest.put("doc", document);
            String json = objectMapper.writeValueAsString(updateRequest);
            HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
            Request request = new Request("POST", "/" + INDEX_NAME + "/_update/" + id);
            request.setEntity(entity);
            restClient.performRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("文档更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文档
     * @param id 文档ID
     */
    public void deleteDocument(String id) {
        try {
            Request request = new Request("DELETE", "/" + INDEX_NAME + "/_doc/" + id);
            restClient.performRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("文档删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索文档
     * @param query 搜索查询
     * @param size 返回数量
     * @return 搜索结果
     */
    public List<Map<String, Object>> searchDocuments(String query, int size) {
        try {
            // 构建搜索请求
            java.util.HashMap<String, Object> searchRequest = new java.util.HashMap<>();
            java.util.HashMap<String, Object> queryMap = new java.util.HashMap<>();
            java.util.HashMap<String, Object> multiMatchMap = new java.util.HashMap<>();
            
            multiMatchMap.put("query", query);
            multiMatchMap.put("fields", java.util.List.of("content"));
            queryMap.put("multi_match", multiMatchMap);
            searchRequest.put("query", queryMap);
            searchRequest.put("size", size);
            
            String json = objectMapper.writeValueAsString(searchRequest);
            HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
            Request request = new Request("POST", "/" + INDEX_NAME + "/_search");
            request.setEntity(entity);
            
            Response response = restClient.performRequest(request);
            InputStream inputStream = response.getEntity().getContent();
            Map<String, Object> result = objectMapper.readValue(inputStream, Map.class);
            
            List<Map<String, Object>> hits = (List<Map<String, Object>>) ((Map<String, Object>) result.get("hits")).get("hits");
            List<Map<String, Object>> results = new ArrayList<>();
            
            for (Map<String, Object> hit : hits) {
                results.add((Map<String, Object>) hit.get("_source"));
            }
            
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("文档搜索失败: " + e.getMessage());
        }
    }
}
