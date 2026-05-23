package org.hongxi.whatsmars.ai.mcp.server;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    /**
     * 根据城市名称获取天气信息
     * description 非常重要，它会告诉 AI 这个工具是做什么的，以及参数代表什么
     */
    @Tool(description = "根据城市名称获取当地的实时天气状况")
    public String getWeatherByCity(String city) {
        // 这里写真实的业务逻辑，比如调用第三方天气API或查询数据库
        return city + " 今天天气晴朗，气温25度！";
    }
}