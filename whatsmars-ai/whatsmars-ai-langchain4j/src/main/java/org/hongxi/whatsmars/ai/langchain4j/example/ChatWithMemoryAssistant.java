package org.hongxi.whatsmars.ai.langchain4j.example;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * 支持多轮对话的 AI 助手
 * <p>
 * 使用 MessageWindowChatMemory 维护对话上下文，最多保留最近 10 条消息
 * </p>
 *
 * @author hongxi
 */
@AiService
public interface ChatWithMemoryAssistant {

    /**
     * 进行多轮对话
     *
     * @param userMessage 用户消息
     * @return AI 回复
     */
    @SystemMessage("你是一个专业的 Java 技术专家，能够记住之前的对话内容。回答要简洁、准确。")
    String chat(String userMessage);

    /**
     * 配置聊天记忆
     * LangChain4j 会自动查找名为 chatMemory 的 Bean
     *
     * @return 聊天记忆实例
     */
    static ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }
}
