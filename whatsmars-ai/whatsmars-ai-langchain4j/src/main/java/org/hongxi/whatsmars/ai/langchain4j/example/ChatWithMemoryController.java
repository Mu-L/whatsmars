package org.hongxi.whatsmars.ai.langchain4j.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 多轮对话控制器
 * <p>
 * 支持基于会话 ID 的多轮对话，每个会话独立维护对话历史
 * </p>
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai/memory")
public class ChatWithMemoryController {

    private static final Logger log = LoggerFactory.getLogger(ChatWithMemoryController.class);

    @Autowired
    private ChatWithMemoryAssistant assistant;

    /**
     * 创建新会话
     *
     * @return 会话 ID
     */
    @PostMapping("/session")
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        log.info("创建新会话: {}", sessionId);
        return sessionId;
    }

    /**
     * 发送消息进行对话
     *
     * @param sessionId 会话 ID
     * @param message   用户消息
     * @return AI 回复
     */
    @PostMapping("/chat")
    public String chat(@RequestParam String sessionId,
                       @RequestParam String message) {
        log.info("会话 [{}] 收到消息: {}", sessionId, message);
        String response = assistant.chat(sessionId, message);
        log.info("会话 [{}] AI 回复: {}", sessionId, response);
        return response;
    }
}
