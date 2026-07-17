package org.hongxi.whatsmars.ai.langchain4j.chatmemory;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对话记忆配置
 * <p>
 * 配置 ChatMemoryProvider，为每个会话维护独立的对话历史。
 * langchain4j-spring-boot-starter 检测到该 Bean 后，
 * 会自动为使用 @MemoryId 的 @AiService 接口提供记忆能力。
 * </p>
 *
 * @author hongxi
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 对话记忆提供者
     * <p>
     * 为每个 memoryId 创建独立的 MessageWindowChatMemory，
     * 保留最近 20 条消息作为上下文窗口。
     * 生产环境可替换为持久化实现（如 Redis / DB）。
     * </p>
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(20);
    }
}
