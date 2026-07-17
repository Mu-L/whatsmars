package org.hongxi.whatsmars.ai.controller;

import org.hongxi.whatsmars.ai.service.ChatMemoryService;
import org.hongxi.whatsmars.ai.vo.ChatRequest;
import org.hongxi.whatsmars.ai.vo.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * ChatMemory 多轮对话控制器
 * <p>
 * 提供基于 JDBC 持久化对话记忆的 REST 接口：
 * <ul>
 *   <li>POST /ai/memory/chat  — 带记忆的多轮对话（相同 conversationId 共享上下文）</li>
 *   <li>DELETE /ai/memory/{conversationId} — 清除指定会话的历史记忆</li>
 * </ul>
 * </p>
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai/memory")
public class ChatMemoryController {

    private static final Logger log = LoggerFactory.getLogger(ChatMemoryController.class);

    private final ChatMemoryService chatMemoryService;

    public ChatMemoryController(ChatMemoryService chatMemoryService) {
        this.chatMemoryService = chatMemoryService;
    }

    /**
     * 带记忆的多轮对话（流式）
     *
     * @param conversationId 会话 ID
     * @param message        用户输入
     * @return 流式 AI 回复
     */
    @GetMapping("/chat")
    public ResponseEntity<Flux<String>> chatStream(
            @RequestParam(required = false, defaultValue = "default") String conversationId,
            @RequestParam String message) {
        log.info("ChatMemory 流式对话请求，conversationId={}", conversationId);
        Flux<String> stream = chatMemoryService.chatStream(conversationId, message);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/event-stream;charset=UTF-8"))
                .header("Cache-Control", "no-cache")
                .body(stream);
    }

    /**
     * 带记忆的多轮对话（非流式，保留兼容）
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String conversationId = request.conversationId() != null ? request.conversationId() : "default";
        log.info("ChatMemory 对话请求，conversationId={}", conversationId);
        String reply = chatMemoryService.chat(conversationId, request.message());
        return ResponseEntity.ok(new ChatResponse(conversationId, reply));
    }

    /**
     * 清除指定会话的对话记忆
     *
     * @param conversationId 会话 ID
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<String> clearMemory(@PathVariable String conversationId) {
        log.info("清除会话记忆，conversationId={}", conversationId);
        chatMemoryService.clearMemory(conversationId);
        return ResponseEntity.ok("会话记忆已清除");
    }
}
