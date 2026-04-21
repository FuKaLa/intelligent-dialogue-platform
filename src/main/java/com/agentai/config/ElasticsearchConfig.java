package com.agentai.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 配置
 */
@Configuration
public class ElasticsearchConfig {
    
    @Value("${middleware.elasticsearch.host}")
    private String host;
    
    @Value("${middleware.elasticsearch.port}")
    private int port;
    
    /**
     * 创建 Elasticsearch REST 客户端实例
     * @return RestClient 实例
     */
    @Bean
    public RestClient restClient() {
        return RestClient.builder(
                new HttpHost(host, port, "http")
        ).build();
    }
}
