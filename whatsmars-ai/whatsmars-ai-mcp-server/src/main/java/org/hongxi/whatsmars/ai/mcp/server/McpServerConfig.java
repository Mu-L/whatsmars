package org.hongxi.whatsmars.ai.mcp.server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {
        // 将带有 @Tool 注解的服务对象传入，Spring AI 会自动提取工具信息
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherService)
                .build();
    }
}