package org.hongxi.whatsmars.ai.openai.example.controller;

import org.hongxi.whatsmars.ai.openai.example.tool.CalculatorTools;
import org.hongxi.whatsmars.ai.openai.example.tool.SearchTools;
import org.hongxi.whatsmars.ai.openai.example.tool.WeatherTools;
import org.hongxi.whatsmars.ai.openai.example.vo.AgentResult;
import org.hongxi.whatsmars.ai.openai.example.vo.ChatResponse;
import org.hongxi.whatsmars.ai.openai.example.vo.DemoResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReAct Agent 控制器
 * <p>
 * ReAct (Reasoning + Acting) 是一种结合推理和行动的 Agent 模式。
 * Agent 会根据任务需求，自主决定调用哪些工具来获取信息或执行操作。
 * </p>
 * <p>
 * 工作流程：
 * 1. 接收用户问题
 * 2. 分析问题，判断是否需要调用工具
 * 3. 选择合适的工具并执行
 * 4. 基于工具返回结果进行推理
 * 5. 生成最终答案
 * </p>
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai/react-agent")
public class ReactAgentController {
    private static final Logger log = LoggerFactory.getLogger(ReactAgentController.class);

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;
    private final SearchTools searchTools;
    private final CalculatorTools calculatorTools;

    public ReactAgentController(ChatClient.Builder builder,
                                WeatherTools weatherTools,
                                SearchTools searchTools,
                                CalculatorTools calculatorTools) {
        this.chatClient = builder.build();
        this.weatherTools = weatherTools;
        this.searchTools = searchTools;
        this.calculatorTools = calculatorTools;
    }

    /**
     * ReAct Agent 智能问答
     * <p>
     * Agent 会自动判断需要调用哪些工具来回答问题
     * </p>
     *
     * <p>
     * 测试示例：
     *     北京今天的天气怎么样？
     *     什么是 Apache Dubbo？
     *     299打8折再减50是多少？
     *     我想了解微服务架构的最新发展趋势
     * </p>
     *
     * @param question 用户问题
     * @return Agent 的回答和思考过程
     */
    @GetMapping("/chat")
    public AgentResult agentChat(@RequestParam String message) {
        log.info("Agent 收到问题: {}", message);
        String response = chatClient.prompt()
                .system("""
                        你是一个智能助手，可以使用各种工具来帮助用户解决问题。
                        
                        你可以使用的工具包括：
                        - 天气查询：获取城市天气信息
                        - 知识搜索：搜索技术主题的相关信息
                        - 最新资讯：获取技术领域的最新动态
                        - 数学计算：执行加减乘除等运算
                        
                        回答要求：
                        1. 根据问题需要，主动调用合适的工具获取信息
                        2. 基于工具返回的结果给出完整、有用的回答
                        3. 如果一个问题需要多个工具配合，依次调用
                        4. 保持回答简洁、准确、有用
                        """)
                .user(message)
                .tools(weatherTools, searchTools, calculatorTools)
                .call()
                .content();
        log.info("Agent 回复: {}", response);
        return new AgentResult(message, response, "react-agent");
    }

    /**
     * 复杂任务处理示例
     * <p>
     * 展示 Agent 如何处理需要多步推理和多个工具调用的复杂任务
     * </p>
     *
     * @param task 复杂任务描述
     * @return 任务执行结果
     */
    @GetMapping("/complex-task")
    public AgentResult handleComplexTask(@RequestParam String message) {
        log.info("Agent 收到复杂任务: {}", message);
        String response = chatClient.prompt()
                .system("""
                        你是一个强大的 AI Agent，擅长解决复杂问题。
                        
                        解决复杂问题的步骤：
                        1. 理解任务目标
                        2. 分解任务为多个子任务
                        3. 对每个子任务选择合适的工具
                        4. 整合所有信息给出最终答案
                        
                        可用的工具：
                        - 天气查询、知识搜索、最新资讯、数学计算
                        
                        请详细展示你的思考过程和每一步的操作结果。
                        """)
                .user(message)
                .tools(weatherTools, searchTools, calculatorTools)
                .call()
                .content();
        log.info("Agent 完成复杂任务");
        return new AgentResult(message, response, "complex-task-solving");
    }

    /**
     * 带上下文的对话
     * <p>
     * 支持多轮对话，Agent 会记住之前的对话内容
     * </p>
     *
     * @param message 当前消息
     * @param contextId 会话ID（用于区分不同会话）
     * @return 回复消息
     */
    @GetMapping("/contextual-chat")
    public ChatResponse contextualChat(@RequestParam String message,
                                              @RequestParam(required = false, defaultValue = "default") String contextId) {
        // 在实际项目中，应该使用 ChatMemory 来管理对话历史
        // 这里简化处理，仅展示概念
        String response = chatClient.prompt()
                .system("""
                        你是一个智能对话助手，可以进行多轮对话。
                        请记住用户之前提到的信息，并在后续对话中合理利用。
                        
                        可用工具：天气查询、知识搜索、最新资讯、数学计算
                        """)
                .user(message)
                .tools(weatherTools, searchTools, calculatorTools)
                .call()
                .content();

        return new ChatResponse(message, response);
    }

    /**
     * Agent 决策演示
     * <p>
     * 展示 Agent 如何根据问题类型自动选择最合适的工具
     * </p>
     *
     * @return 多个示例的对比结果
     */
    @GetMapping("/demo")
    public DemoResult demo() {

        // 示例 1: 需要天气查询
        String weatherAnswer = chatClient.prompt()
                .user("北京今天的天气怎么样？我需要出门，应该穿什么衣服？")
                .tools(weatherTools, searchTools, calculatorTools)
                .call()
                .content();
        var weatherExample = new ChatResponse(
                "北京今天的天气怎么样？我需要出门，应该穿什么衣服？",
                weatherAnswer
        );
        
        // 示例 2: 需要知识搜索
        String searchAnswer = chatClient.prompt()
                .user("什么是 Apache Dubbo？它有什么特点？")
                .tools(weatherTools, searchTools, calculatorTools)
                .call()
                .content();
        var searchExample = new ChatResponse(
                "什么是 Apache Dubbo？它有什么特点？",
                searchAnswer
        );
        
        // 示例 3: 需要数学计算
        String calcAnswer = chatClient.prompt()
                .user("如果一个商品原价 299 元，打 8 折后再减 50 元优惠券，最终价格是多少？")
                .tools(weatherTools, searchTools, calculatorTools)
                .call()
                .content();
        var calculationExample = new ChatResponse(
                "如果一个商品原价 299 元，打 8 折后再减 50 元优惠券，最终价格是多少？",
                calcAnswer
        );
        
        // 示例 4: 需要综合多个工具
        String complexAnswer = chatClient.prompt()
                .user("我想了解微服务架构的最新发展趋势，以及 Spring Boot 在其中的作用")
                .tools(weatherTools, searchTools, calculatorTools)
                .call()
                .content();
        var complexExample = new ChatResponse(
                "我想了解微服务架构的最新发展趋势，以及 Spring Boot 在其中的作用",
                complexAnswer
        );
        
        return new DemoResult(weatherExample, searchExample, calculationExample, complexExample);
    }
}
