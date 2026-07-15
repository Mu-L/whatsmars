package org.hongxi.whatsmars.ai.alibaba.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;

import org.hongxi.whatsmars.ai.alibaba.tool.WhatsMarsTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI Alibaba ReactAgent 配置
 * <p>
 * 演示如何使用 ReactAgent 构建智能体，并注册自定义工具。
 * 同时通过 MCP Client 连接 ai-spring 的 MCP Server，
 * 将远程 MCP 工具与本地工具合并后提供给 Agent 使用。
 */
@Configuration
public class AgentConfig {

    private static final String INSTRUCTION = """
            You are a helpful assistant named WhatsMars AI.
            You have access to both local tools and remote MCP tools:
            - Local tools: get current date/time, text processing (uppercase/lowercase/reverse)
            - MCP remote tools: weather query, math calculation, URL encode/decode, Base64 encode/decode, JSON formatting, etc.
            Use these tools to assist users with their tasks.
            Always respond in Chinese.
            """;

    @Bean
    public ReactAgent chatbotReactAgent(ChatModel chatModel,
                                        WhatsMarsTools whatsMarsTools,
                                        ToolCallbackProvider mcpToolCallbacks,
                                        MemorySaver memorySaver) {
        // 本地工具
        ToolCallbackProvider localProvider = MethodToolCallbackProvider.builder()
                .toolObjects(whatsMarsTools)
                .build();

        // 合并本地工具 + MCP 远程工具
        List<ToolCallback> allTools = new ArrayList<>();
        allTools.addAll(List.of(localProvider.getToolCallbacks()));
        allTools.addAll(List.of(mcpToolCallbacks.getToolCallbacks()));

        return ReactAgent.builder()
                .name("WhatsMarsAgent")
                .model(chatModel)
                .instruction(INSTRUCTION)
                .enableLogging(true)
                .saver(memorySaver)
                .tools(allTools.toArray(new ToolCallback[0]))
                .build();
    }

    @Bean
    public MemorySaver memorySaver() {
        return new MemorySaver();
    }
}
