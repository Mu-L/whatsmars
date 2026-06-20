package org.hongxi.whatsmars.ai.openai.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 流式聊天控制器
 * <p>
 * 演示如何使用 Spring AI 实现流式响应
 * </p>
 *
 * @author hongxi
 */
@RestController
public class QwenStreamController {

    private static final Logger log = LoggerFactory.getLogger(QwenStreamController.class);

    private final ChatClient chatClient;

    public QwenStreamController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 流式聊天接口
     * <p>
     * 浏览器访问会乱码，可以用 curl 测试：
     * curl "http://localhost:8080/ai/stream-chat?input=%E6%AD%A6%E6%B1%89%E7%AE%80%E4%BB%8B"
     * </p>
     *
     * @param input 用户输入
     * @return 流式响应
     */
    @GetMapping(value = "/ai/stream-chat", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> streamChat(@RequestParam String input) {
        log.info("开始流式对话: {}", input);
        
        // 使用 stream() 方法，返回 Flux 流式数据
        return chatClient.prompt()
                .user(input)
                .stream()
                .content()
                .doOnNext(chunk -> log.debug("收到 chunk: {}", chunk))
                .doOnComplete(() -> log.info("流式对话完成"));
    }
}
