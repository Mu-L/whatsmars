package org.hongxi.whatsmars.ai.mcp.server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP Server 配置类
 * <p>
 * 注册所有 Tool 服务，使其可被 AI 调用
 * </p>
 *
 * @author hongxi
 */
@Configuration
public class McpServerConfig {

    /**
     * 注册天气工具
     */
    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherService)
                .build();
    }

    /**
     * 注册系统工具
     */
    @Bean
    public ToolCallbackProvider systemTools(SystemToolService systemToolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(systemToolService)
                .build();
    }

    /**
     * 注册数据转换工具
     */
    @Bean
    public ToolCallbackProvider conversionTools(ConversionToolService conversionToolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(conversionToolService)
                .build();
    }
}