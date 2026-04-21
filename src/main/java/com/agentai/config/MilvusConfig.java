package com.agentai.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量数据库配置
 */
@Configuration
public class MilvusConfig {
    
    @Value("${middleware.milvus.host}")
    private String host;
    
    @Value("${middleware.milvus.port}")
    private int port;
    
    /**
     * 创建 Milvus 客户端实例
     * @return MilvusClientV2 实例
     */
    @Bean
    public MilvusClientV2 milvusClientV2() {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri("http://" + host + ":" + port)
                .build();
        return new MilvusClientV2(connectConfig);
    }
}
