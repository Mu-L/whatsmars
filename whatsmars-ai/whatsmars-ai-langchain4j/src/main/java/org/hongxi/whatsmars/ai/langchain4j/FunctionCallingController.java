package org.hongxi.whatsmars.ai.langchain4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 函数调用控制器
 * <p>
 * 演示 AI 如何自动调用工具函数
 * </p>
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai/function")
public class FunctionCallingController {

    private static final Logger log = LoggerFactory.getLogger(FunctionCallingController.class);

    @Autowired
    private FunctionCallingAssistant assistant;

    /**
     * 发送消息，AI 会根据需要调用工具
     * <p>
     * 测试示例：
     * - "现在几点了？" -> 调用 getCurrentTime()
     * - "今天日期是多少？" -> 调用 getCurrentDate()
     * - "计算 123 + 456" -> 调用 add(123, 456)
     * - "北京天气怎么样？" -> 调用 getWeather("北京")
     * </p>
     *
     * @param message 用户消息
     * @return AI 回复
     */
    @RequestMapping("/chat")
    public String chat(@RequestParam String message) {
        log.info("收到消息: {}", message);
        String response = assistant.chat(message);
        log.info("AI 回复: {}", response);
        return response;
    }
}
