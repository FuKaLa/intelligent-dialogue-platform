package com.agentai.common;

import io.milvus.client.MilvusClientV2;
import io.milvus.grpc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Milvus 向量客户端
 * 用于向量的存储和检索，支持向量索引配置
 * 
 * <p>主要功能：
 * 1. 初始化 Milvus 集合
 * 2. 管理向量索引
 * 3. 执行向量插入和检索操作
 * 4. 提供向量相似度搜索
 * </p>
 * 
 * <p>技术特点：
 * - 使用 Milvus SDK 2.4.4
 * - 支持 IVF_FLAT 索引类型
 * - 支持批量向量操作
 * - 提供异常处理和错误恢复
 * </p>
 */
@Component
public class MilvusVectorClient {
    
    /**
     * Milvus 客户端实例
     */
    @Autowired
    private MilvusClientV2 milvusClientV2;
    
    /**
     * 集合名称
     */
    private static final String COLLECTION_NAME = "intelligent_dialogue_platform";
    
    /**
     * 向量维度
     */
    private static final int DIMENSION = 768;
    
    /**
     * 初始化 Milvus 集合
     * 
     * <p>初始化流程：
     * 1. 创建集合（如果不存在）
     * 2. 加载集合到内存
     * 3. 忽略已存在的集合错误
     * </p>
     */
    @PostConstruct
    public void init() {
        try {
            // 创建集合（快速设置模式）
            CreateCollectionReq createCollectionReq = CreateCollectionReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .dimension(DIMENSION)
                    .build();
            
            milvusClientV2.createCollection(createCollectionReq);
            
            // 加载集合到内存
            LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .build();
            
            milvusClientV2.loadCollection(loadCollectionReq);
        } catch (Exception e) {
            // 如果集合已存在，忽略错误
            if (!e.getMessage().contains("already exists")) {
                e.printStackTrace();
                throw new BusinessException("Milvus 初始化失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 插入向量
     * 
     * @param vectors 向量列表
     * @param ids ID 列表
     * @param texts 文本列表
     * @return 插入结果
     * 
     * <p>插入流程：
     * 1. 构建插入请求
     * 2. 添加向量字段
     * 3. 添加文本字段
     * 4. 执行插入操作
     * 5. 返回插入结果
     * </p>
     */
    public InsertResp insertVectors(List<List<Float>> vectors, List<Long> ids, List<String> texts) {
        try {
            InsertReq insertReq = InsertReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .build();
            
            // 添加向量字段
            List<Float> floatVectors = new ArrayList<>();
            for (List<Float> vector : vectors) {
                floatVectors.addAll(vector);
            }
            
            // 构建字段数据
            FieldData floatField = FieldData.newBuilder()
                    .setScalars(ScalarField.newBuilder()
                            .setFloatVector(FloatVector.newBuilder()
                                    .addAllData(floatVectors)
                                    .setDim(DIMENSION)
                                    .build())
                            .build())
                    .build();
            
            // 构建文本字段
            FieldData textField = FieldData.newBuilder()
                    .setScalars(ScalarField.newBuilder()
                            .setStringData(StringData.newBuilder()
                                    .addAllData(texts)
                                    .build())
                            .build())
                    .build();
            
            // 构建 ID 字段
            FieldData idField = FieldData.newBuilder()
                    .setScalars(ScalarField.newBuilder()
                            .setLongData(LongData.newBuilder()
                                    .addAllData(ids)
                                    .build())
                            .build())
                    .build();
            
            // 添加字段到请求
            insertReq.addFields(floatField);
            insertReq.addFields(textField);
            insertReq.addFields(idField);
            
            // 执行插入
            return milvusClientV2.insert(insertReq);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("向量插入失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索向量
     * 
     * @param queryVector 查询向量
     * @param topK 返回结果数量
     * @return 搜索结果
     * 
     * <p>搜索流程：
     * 1. 构建搜索请求
     * 2. 设置搜索参数
     * 3. 执行搜索操作
     * 4. 处理搜索结果
     * </p>
     */
    public SearchResults searchVectors(List<Float> queryVector, int topK) {
        try {
            SearchReq searchReq = SearchReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .build();
            
            // 构建搜索参数
            SearchParam searchParam = SearchParam.newBuilder()
                    .setTopK(topK)
                    .setMetricType(MetricType.IP)
                    .setParams("{\"nprobe\": 10}")
                    .build();
            
            // 添加查询向量
            List<Float> floatVectors = new ArrayList<>(queryVector);
            FieldData floatField = FieldData.newBuilder()
                    .setScalars(ScalarField.newBuilder()
                            .setFloatVector(FloatVector.newBuilder()
                                    .addAllData(floatVectors)
                                    .setDim(DIMENSION)
                                    .build())
                            .build())
                    .build();
            
            // 添加字段和参数到请求
            searchReq.addFields(floatField);
            searchReq.addSearchParams(searchParam);
            
            // 执行搜索
            return milvusClientV2.search(searchReq);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("向量搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建向量索引
     * 
     * <p>索引配置：
     * - 索引类型：IVF_FLAT
     * - nlist：128
     * - 适用于中小规模数据集
     * </p>
     */
    public void createIndex() {
        try {
            CreateIndexReq createIndexReq = CreateIndexReq.builder()
                    .collectionName(COLLECTION_NAME)
                    .fieldName("vector")
                    .indexName("vector_index")
                    .indexType(IndexType.IVF_FLAT)
                    .params("{\"nlist\": 128}")
                    .build();
            
            milvusClientV2.createIndex(createIndexReq);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("索引创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取集合名称
     * 
     * @return 集合名称
     */
    public String getCollectionName() {
        return COLLECTION_NAME;
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
