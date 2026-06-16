package org.hongxi.whatsmars.ai.langchain4j.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public Map<String, String> createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, "新建会话");
        log.info("创建新会话: {}", sessionId);

        Map<String, String> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("message", "会话创建成功");
        return result;
    }

    /**
     * 发送消息进行对话
     *
     * @param sessionId 会话 ID
     * @param message   用户消息
     * @return AI 回复
     */
    @PostMapping("/chat")
    public Map<String, String> chat(@RequestParam String sessionId,
                                     @RequestParam String message) {
        if (!sessionStore.containsKey(sessionId)) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }

        log.info("会话 [{}] 收到消息: {}", sessionId, message);
        String response = assistant.chat(message);
        log.info("会话 [{}] AI 回复: {}", sessionId, response);

        Map<String, String> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("userMessage", message);
        result.put("aiResponse", response);
        return result;
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话 ID
     * @return 操作结果
     */
    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> deleteSession(@PathVariable String sessionId) {
        sessionStore.remove(sessionId);
        log.info("删除会话: {}", sessionId);

        Map<String, String> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("message", "会话已删除");
        return result;
    }
}
