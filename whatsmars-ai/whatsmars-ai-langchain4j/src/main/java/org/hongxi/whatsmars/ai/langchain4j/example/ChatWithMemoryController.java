package org.hongxi.whatsmars.ai.langchain4j.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
     * 简单的会话存储（生产环境建议使用 Redis）
     * key: sessionId, value: 会话描述
     */
    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    /**
     * 创建新会话
     *
     * @return 会话 ID
     */
    @PostMapping("/session")
    public Object createSession() {
        record SessionCreated(String sessionId, String message) {}

        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, "新建会话");
        log.info("创建新会话: {}", sessionId);

        return new SessionCreated(sessionId, "会话创建成功");
    }

    /**
     * 发送消息进行对话
     *
     * @param sessionId 会话 ID
     * @param message   用户消息
     * @return AI 回复
     */
    @PostMapping("/chat")
    public Object chat(@RequestParam String sessionId,
                       @RequestParam String message) {
        record ChatResult(String sessionId, String userMessage, String aiResponse) {}

        if (!sessionStore.containsKey(sessionId)) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        log.info("会话 [{}] 收到消息: {}", sessionId, message);
        String response = assistant.chat(message);
        log.info("会话 [{}] AI 回复: {}", sessionId, response);

        return new ChatResult(sessionId, message, response);
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话 ID
     * @return 操作结果
     */
    @DeleteMapping("/session/{sessionId}")
    public Object deleteSession(@PathVariable String sessionId) {
        record SessionDeleted(String sessionId, String message) {}

        sessionStore.remove(sessionId);
        log.info("删除会话: {}", sessionId);

        return new SessionDeleted(sessionId, "会话已删除");
    }
}
