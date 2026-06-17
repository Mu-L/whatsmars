package org.hongxi.whatsmars.ai.openai.example.controller;

import org.hongxi.whatsmars.ai.openai.example.tool.TimeTools;
import org.hongxi.whatsmars.ai.openai.example.tool.UserTools;
import org.hongxi.whatsmars.ai.openai.example.tool.WeatherTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Function Calling（函数调用）示例控制器
 * <p>
 * 演示如何使用 @Tool 注解让 AI 模型自动调用 Java 方法来获取实时数据
 * </p>
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai/fc")
public class FunctionCallingController {

    private static final Logger log = LoggerFactory.getLogger(FunctionCallingController.class);

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;
    private final TimeTools timeTools;
    private final UserTools userTools;

    public FunctionCallingController(
            ChatClient.Builder builder,
            WeatherTools weatherTools,
            TimeTools timeTools,
            UserTools userTools) {
        this.chatClient = builder.build();
        this.weatherTools = weatherTools;
        this.timeTools = timeTools;
        this.userTools = userTools;
    }

    /**
     * 天气查询 - 使用函数调用
     * <p>
     * 测试示例: "北京今天的天气怎么样？"
     * </p>
     *
     * @param question 用户问题
     * @return AI 回复
     */
    @GetMapping("/weather")
    public Map<String, Object> getWeather(@RequestParam String question) {
        log.info("天气查询: {}", question);

        String response = chatClient.prompt()
                .user(question)
                .tools(weatherTools, timeTools, userTools)
                .call()
                .content();

        log.info("AI 回复: {}", response);

        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("answer", response);
        return result;
    }

    /**
     * 智能助手 - 自动选择需要调用的函数
     * <p>
     * 测试示例: 
     * - "帮我查一下上海的天气"
     * - "现在几点了？"
     * - "我叫李四，今年30岁，请介绍一下我"
     * </p>
     *
     * @param question 用户问题
     * @return AI 回复
     */
    @GetMapping("/ask")
    public Map<String, Object> smartAssistant(@RequestParam String question) {
        log.info("智能助手收到问题: {}", question);

        String response = chatClient.prompt()
                .system("你是一个智能助手，可以根据用户的问题自动调用合适的工具来获取信息。")
                .user(question)
                .tools(weatherTools, timeTools, userTools)
                .call()
                .content();

        log.info("AI 回复: {}", response);

        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("answer", response);
        return result;
    }
}
