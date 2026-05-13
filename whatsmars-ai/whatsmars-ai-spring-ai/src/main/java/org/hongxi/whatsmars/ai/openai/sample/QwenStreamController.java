package org.hongxi.whatsmars.ai.openai.sample;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class QwenStreamController {

    private final ChatClient chatClient;

    public QwenStreamController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 浏览器访问会乱码，可以用curl测试
     * curl "http://localhost:8080/ai/stream-chat?input=%E6%AD%A6%E6%B1%89%E7%AE%80%E4%BB%8B"
     */
    @GetMapping("/ai/stream-chat")
    public Flux<String> streamChat(@RequestParam String input) {
        // 使用 stream() 方法，返回 Flux 流式数据
        return chatClient.prompt()
                .user(input)
                .stream()
                .content();
    }
}