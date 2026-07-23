package org.hongxi.whatsmars.ai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 基础聊天控制器
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai")
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 简单聊天接口
     */
    @RequestMapping("/chat")
    public String chat(@RequestParam String message) {
        log.info("收到聊天请求: {}", message);
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @RequestMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
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
    @GetMapping("/chat/stream2")
    public ResponseEntity<Flux<String>> streamChat2(@RequestParam String message) {
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

    /**
     * 结构化输出示例
     * <p>
     * eg. 我叫张三，今年25岁，是一名软件工程师，喜欢编程和打篮球，邮箱是zhangsan@example.com
     * </p>
     */
    @RequestMapping("/extract")
    public Object extractPersonInfo(@RequestParam String message) {
        record PersonInfo(String name, Integer age, String email, String occupation) {}
        log.info("提取人员信息: {}", message);
        String prompt = """
                从以下文本中提取人员信息，并以 JSON 格式返回：
                %s
                
                需要提取的字段：name(姓名), age(年龄), email(邮箱), occupation(职业)
                如果某个字段无法提取，请设置为 null。
                """.formatted(message);
        return chatClient.prompt(new Prompt(prompt))
                .call()
                .entity(PersonInfo.class);
    }
}
