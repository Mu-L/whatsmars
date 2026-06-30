package org.hongxi.whatsmars.ai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * OpenAI 聊天控制器
 * <p>
 * 演示如何使用 Spring AI 实现简单聊天和流式聊天
 * </p>
 *
 * @author hongxi
 */
@RestController
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 简单聊天接口
     */
    @RequestMapping("/ai/chat")
    public String chat(@RequestParam String message) {
        log.info("收到聊天请求: {}", message);
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式聊天接口
     *
     * <p>
     *     浏览器访问会乱码，可用 curl 测试
     *     Spring Boot 4.0 在浏览器访问不会乱码
     * </p>
     *
     * @param message 用户输入
     * @return 流式响应
     */
    @GetMapping("/ai/chat/stream")
    public ResponseEntity<Flux<String>> streamChat(@RequestParam String message) {
        log.info("开始流式对话: {}", message);
        
        // 使用 stream() 方法，返回 Flux 流式数据
        Flux<String> stream = chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .doOnNext(chunk -> log.debug("收到 chunk: {}", chunk))
                .doOnComplete(() -> log.info("流式对话完成"));

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/event-stream;charset=UTF-8"))
                .header("Cache-Control", "no-cache")
                .body(stream);
    }
}
