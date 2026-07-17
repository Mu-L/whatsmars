package org.hongxi.whatsmars.ai.service;

import org.hongxi.whatsmars.ai.tool.TimeTools;
import org.hongxi.whatsmars.ai.tool.UserTools;
import org.hongxi.whatsmars.ai.tool.WeatherTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Tool Calling（工具调用）服务
 *
 * <p>
 * 工作流程：
 * 1. 用户发送问题
 * 2. AI 模型分析问题，判断是否需要调用工具
 * 3. 如果需要，AI 自动生成工具调用请求（函数名 + 参数）
 * 4. Spring AI 执行对应的 Java 方法，将结果返回给 AI
 * 5. AI 基于工具返回的结果生成最终回答
 * </p>
 *
 * @author hongxi
 */
@Service
public class ToolCallingService {

    private static final Logger log = LoggerFactory.getLogger(ToolCallingService.class);

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;
    private final TimeTools timeTools;
    private final UserTools userTools;

    public ToolCallingService(
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
     * 天气查询 - AI 自动调用天气工具
     * <p>
     * 测试示例: "北京今天的天气怎么样？"
     * </p>
     *
     * @param message 用户问题
     * @return AI 回复
     */
    public String getWeather(String message) {
        log.info("天气查询: {}", message);

        String response = chatClient.prompt()
                .user(message)
                .tools(weatherTools)
                .call()
                .content();

        log.info("AI 回复: {}", response);

        return response;
    }

    /**
     * 时间查询 - AI 自动调用时间工具
     * <p>
     * 测试示例: "现在几点了？" / "今天星期几？" / "距离国庆节还有多少天？"
     * </p>
     *
     * @param message 用户问题
     * @return AI 回复
     */
    public String getTime(String message) {
        log.info("时间查询: {}", message);

        String response = chatClient.prompt()
                .user(message)
                .tools(timeTools)
                .call()
                .content();

        log.info("AI 回复: {}", response);

        return response;
    }

    /**
     * 智能助手 - 自动选择合适的工具
     * <p>
     * AI 会根据问题自动选择调用哪些工具：
     * - "帮我查一下上海的天气" → 调用 WeatherTools
     * - "现在几点了？" → 调用 TimeTools
     * - "介绍一下张三" → 调用 UserTools
     * </p>
     *
     * @param message 用户问题
     * @return AI 回复
     */
    public String smartAssistant(String message) {
        log.info("智能助手收到问题: {}", message);

        String response = chatClient.prompt()
                .system("你是一个智能助手，可以根据用户的问题自动调用合适的工具来获取信息。请用中文回答。")
                .user(message)
                .tools(weatherTools, timeTools, userTools)
                .call()
                .content();

        log.info("AI 回复: {}", response);

        return response;
    }

    /**
     * 天气查询 - 流式
     */
    public Flux<String> getWeatherStream(String message) {
        log.info("天气流式查询: {}", message);
        return chatClient.prompt()
                .user(message)
                .tools(weatherTools)
                .stream()
                .content();
    }

    /**
     * 时间查询 - 流式
     */
    public Flux<String> getTimeStream(String message) {
        log.info("时间流式查询: {}", message);
        return chatClient.prompt()
                .user(message)
                .tools(timeTools)
                .stream()
                .content();
    }

    /**
     * 智能助手 - 流式
     */
    public Flux<String> smartAssistantStream(String message) {
        log.info("智能助手流式收到问题: {}", message);
        return chatClient.prompt()
                .system("你是一个智能助手，可以根据用户的问题自动调用合适的工具来获取信息。请用中文回答。")
                .user(message)
                .tools(weatherTools, timeTools, userTools)
                .stream()
                .content();
    }
}
