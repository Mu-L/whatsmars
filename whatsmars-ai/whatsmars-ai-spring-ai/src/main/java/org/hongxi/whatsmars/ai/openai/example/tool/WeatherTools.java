package org.hongxi.whatsmars.ai.openai.example.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 天气工具类
 * <p>
 * 使用 @Tool 注解定义 AI 可调用的工具函数
 * </p>
 *
 * @author hongxi
 */
@Component
public class WeatherTools {

    /**
     * 获取指定城市的天气信息
     *
     * @param city 城市名称，例如：北京、上海、广州、深圳
     * @return 天气描述
     */
    @Tool(description = "获取指定城市的当前天气信息")
    public String getWeather(@ToolParam(description = "城市名称，例如：北京、上海、广州、深圳") String city) {
        // 模拟天气数据（实际项目中可以调用真实天气 API）
        return switch (city) {
            case "北京" -> "晴天，温度 25°C，空气质量良好";
            case "上海" -> "多云，温度 28°C，湿度 65%";
            case "广州" -> "小雨，温度 30°C，注意带伞";
            case "深圳" -> "晴天，温度 29°C，适合出行";
            default -> "未知城市，无法提供天气信息";
        };
    }
}
