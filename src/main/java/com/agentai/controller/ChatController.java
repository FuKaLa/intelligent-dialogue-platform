package com.agentai.controller;

import com.agentai.common.ResponseResult;
import com.agentai.mcp.ModelControlPlane;
import com.agentai.nlp.TextNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 对话控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatController {
    
    @Autowired
    private TextNormalizer textNormalizer;
    
    @Autowired
    private ModelControlPlane modelControlPlane;
    
    /**
     * 统一对话接口
     */
    @PostMapping("/ask")
    public ResponseResult<?> ask(@RequestBody ChatRequest request) {
        // 1. 文本归一化
        String normalizedText = textNormalizer.normalize(request.getMessage());
        
        // 2. 获取对话ID
        String conversationId = request.getConversationId();
        
        // 3. 处理对话请求
        Map<String, Object> result = modelControlPlane.processDialogue(conversationId, normalizedText);
        
        // 4. 获取生成的回复
        String response = (String) result.get("response");
        
        return ResponseResult.success(response);
    }
}