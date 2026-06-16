package org.hongxi.whatsmars.ai.openai.example.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 时间工具类
 * <p>
 * 使用 @Tool 注解定义 AI 可调用的时间查询函数
 * </p>
 *
 * @author hongxi
 */
@Component
public class TimeTools {

    /**
     * 获取当前的日期和时间
     *
     * @return 当前时间字符串
     */
    @Tool(description = "获取当前的日期和时间")
    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return "当前时间是：" + now;
    }
}
