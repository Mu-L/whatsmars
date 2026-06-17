package org.hongxi.whatsmars.ai.openai.example.cache;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 带缓存的 AI 聊天服务
 * <p>
 * 使用 Spring Cache 为 AI 聊天响应提供缓存支持，避免重复调用大模型 API，
 * 降低延迟和成本。
 * </p>
 * <p>
 * 缓存策略：
 * - 相同的问题在缓存有效期内直接返回缓存结果
 * - 支持手动清除特定缓存或全部缓存
 * - 可针对不同场景配置不同的 TTL
 * </p>
 *
 * @author hongxi
 */
@Service
public class CachedChatService {

    private final ChatClient chatClient;

    public CachedChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 带缓存的聊天请求
     * <p>
     * 使用问题内容作为缓存 key，相同问题在缓存有效期内直接返回缓存结果
     * </p>
     *
     * @param question 用户问题
     * @return AI 回答
     */
    @Cacheable(value = "ai-chat", key = "#question", unless = "#result == null || #result.isEmpty()")
    public String chatWithCache(String question) {
        // 这个方法只在缓存未命中时执行
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * 带系统提示词的缓存聊天
     *
     * @param systemPrompt 系统提示词
     * @param question 用户问题
     * @return AI 回答
     */
    @Cacheable(value = "ai-chat", key = "#systemPrompt + '::' + #question", 
               unless = "#result == null || #result.isEmpty()")
    public String chatWithSystemAndCache(String systemPrompt, String question) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();
    }

    /**
     * 带上下文的缓存聊天（多轮对话）
     *
     * @param contextId 会话ID
     * @param question 当前问题
     * @param history 对话历史
     * @return AI 回答
     */
    @Cacheable(value = "ai-chat", key = "#contextId + ':' + #question", 
               unless = "#result == null || #result.isEmpty()")
    public String contextualChatWithCache(String contextId, String question, List<String> history) {
        // 简化处理：实际项目中应该使用 ChatMemory
        return chatClient.prompt()
                .user(buildContextualPrompt(history, question))
                .call()
                .content();
    }

    /**
     * RAG 问答缓存
     * <p>
     * RAG 检索结果相对稳定，可以设置较长的缓存时间
     * </p>
     *
     * @param question 用户问题
     * @param context 检索到的上下文
     * @return AI 回答
     */
    @Cacheable(value = "ai-rag", key = "#question", unless = "#result == null || #result.isEmpty()")
    public String ragChatWithCache(String question, String context) {
        return chatClient.prompt()
                .system("基于以下上下文回答问题：\n" + context)
                .user(question)
                .call()
                .content();
    }

    /**
     * 更新缓存（强制刷新）
     * <p>
     * 当需要主动更新缓存时使用
     * </p>
     *
     * @param question 问题
     * @param answer 新的回答
     * @return 回答
     */
    @CachePut(value = "ai-chat", key = "#question")
    public String updateCache(String question, String answer) {
        return answer;
    }

    /**
     * 清除指定问题的缓存
     *
     * @param question 问题内容
     */
    @CacheEvict(value = "ai-chat", key = "#question")
    public void evictQuestionCache(String question) {
        // 清除指定问题的缓存
    }

    /**
     * 清除所有聊天缓存
     */
    @CacheEvict(value = "ai-chat", allEntries = true)
    public void clearAllChatCache() {
        // 清除所有聊天缓存
    }

    /**
     * 清除 RAG 缓存
     */
    @CacheEvict(value = "ai-rag", allEntries = true)
    public void clearRagCache() {
        // 清除所有 RAG 缓存
    }

    /**
     * 构建带上下文的提示词
     */
    private String buildContextualPrompt(List<String> history, String currentQuestion) {
        if (history == null || history.isEmpty()) {
            return currentQuestion;
        }
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("对话历史：\n");
        for (int i = 0; i < history.size(); i++) {
            prompt.append((i % 2 == 0 ? "用户: " : "助手: ")).append(history.get(i)).append("\n");
        }
        prompt.append("\n当前问题: ").append(currentQuestion);
        
        return prompt.toString();
    }
}
