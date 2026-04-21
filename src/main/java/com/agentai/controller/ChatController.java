package com.agentai.controller;

import com.agentai.common.ResponseResult;
import com.agentai.dst.DialogueStateManager;
import com.agentai.mcp.ModelControlPlane;
import com.agentai.nlp.EntityExtractor;
import com.agentai.nlp.IntentRecognizer;
import com.agentai.nlp.NLGService;
import com.agentai.nlp.TextNormalizer;
import com.agentai.skill.SkillManager;
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
    private IntentRecognizer intentRecognizer;
    
    @Autowired
    private EntityExtractor entityExtractor;
    
    @Autowired
    private DialogueStateManager dialogueStateManager;
    
    @Autowired
    private ModelControlPlane modelControlPlane;
    
    @Autowired
    private SkillManager skillManager;
    
    @Autowired
    private NLGService nlgService;
    
    /**
     * 统一对话接口
     */
    @PostMapping("/ask")
    public ResponseResult<?> ask(@RequestBody ChatRequest request) {
        // 1. 文本归一化
        String normalizedText = textNormalizer.normalize(request.getMessage());
        
        // 2. 获取对话上下文
        String conversationId = request.getConversationId();
        String context = dialogueStateManager.getHistory(conversationId);
        
        // 3. 意图识别
        String intent = intentRecognizer.recognize(normalizedText);
        
        // 4. 实体抽取和槽位填充
        Map<String, Object> entities = entityExtractor.extract(normalizedText);
        
        // 5. 执行技能
        String skillResponse = skillManager.executeSkill(intent, entities);
        
        // 6. 生成回复
        String response = nlgService.generateResponse(intent, entities, context);
        
        // 7. 更新对话上下文
        dialogueStateManager.addHistory(conversationId, normalizedText, response);
        
        return ResponseResult.success(response);
    }
}