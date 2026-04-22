package com.agentai.controller;

import com.agentai.common.ResponseResult;
import com.agentai.mcp.ModelControlPlane;
import com.agentai.nlp.TextNormalizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

/**
 * 对话控制器
 * 处理用户对话请求，执行全流程处理
 */
@RestController
@RequestMapping("/chat")
@Validated
public class ChatController {
    
    @Autowired
    private TextNormalizer textNormalizer;
    
    @Autowired
    private ModelControlPlane modelControlPlane;
    
    /**
     * 统一对话接口
     * @param request 对话请求，包含用户消息和对话ID
     * @return 统一格式的响应结果
     */
    @PostMapping("/ask")
    public ResponseResult<?> ask(@Valid @RequestBody ChatRequest request) {
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
