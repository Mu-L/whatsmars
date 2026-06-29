package org.hongxi.whatsmars.ai.openai.example.controller;

import org.hongxi.whatsmars.ai.openai.example.cache.CachedChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 缓存功能演示控制器
 * <p>
 * 展示如何使用缓存优化 AI 响应性能，降低 API 调用成本
 * </p>
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai/cache")
public class CacheController {

    private final CachedChatService cachedChatService;

    public CacheController(CachedChatService cachedChatService) {
        this.cachedChatService = cachedChatService;
    }

    /**
     * 带缓存的聊天
     * <p>
     * 第一次调用会请求 AI API，后续相同问题直接返回缓存结果
     * </p>
     *
     * @param message 用户问题
     * @return AI 回答和缓存状态
     */
    @GetMapping("/chat")
    public String cachedChat(@RequestParam String message) {
        long startTime = System.currentTimeMillis();
        String answer = cachedChatService.chatWithCache(message);
        long duration = System.currentTimeMillis() - startTime;
        return answer;
    }

    /**
     * 带系统提示词的缓存聊天
     *
     * @param systemPrompt 系统提示词
     * @param message 用户问题
     * @return AI 回答
     */
    @PostMapping("/chat-with-system")
    public String chatWithSystem(
            @RequestParam String systemPrompt,
            @RequestParam String message) {
        return cachedChatService.chatWithSystemAndCache(systemPrompt, message);
    }

    /**
     * RAG 问答（带缓存）
     *
     * @param message 用户问题
     * @param context 检索到的上下文
     * @return AI 回答
     */
    @PostMapping("/rag-chat")
    public String ragChat(@RequestParam String message,
                                       @RequestParam String context) {
        return cachedChatService.ragChatWithCache(message, context);
    }

    /**
     * 清除指定问题的缓存
     *
     * @param message 问题内容
     * @return 操作结果
     */
    @DeleteMapping("/evict")
    public String evictCache(@RequestParam String message) {
        cachedChatService.evictQuestionCache(message);
        return "已清除问题 \"" + message + "\" 的缓存";
    }

    /**
     * 清除所有聊天缓存
     *
     * @return 操作结果
     */
    @DeleteMapping("/clear-all")
    public String clearAllCache() {
        cachedChatService.clearAllChatCache();
        return "已清除所有聊天缓存";
    }

    /**
     * 缓存性能对比测试
     * <p>
     * 连续调用两次相同问题，对比响应时间
     * </p>
     *
     * @param message 测试问题
     * @return 性能对比结果
     */
    @GetMapping("/benchmark")
    public String benchmark(@RequestParam String message) {
        // 第一次调用（缓存未命中）
        long start1 = System.currentTimeMillis();
        String answer1 = cachedChatService.chatWithCache(message);
        long duration1 = System.currentTimeMillis() - start1;
        
        // 第二次调用（缓存命中）
        long start2 = System.currentTimeMillis();
        String answer2 = cachedChatService.chatWithCache(message);
        long duration2 = System.currentTimeMillis() - start2;
        
        return """
                问题: %s
                
                第一次调用: %dms (缓存未命中)
                回答: %s
                
                第二次调用: %dms (缓存命中)
                回答: %s
                
                性能提升: %dms (%.2fx)
                """.formatted(
                message,
                duration1, answer1,
                duration2, answer2,
                (duration1 - duration2),
                duration2 > 0 ? (double) duration1 / duration2 : 0.0
        );
    }

    /**
     * 多轮对话缓存示例
     *
     * @param contextId 会话ID
     * @param message 当前问题
     * @param history 对话历史（JSON 数组）
     * @return AI 回答
     */
    @PostMapping("/contextual-chat")
    public String contextualChat(
            @RequestParam String contextId,
            @RequestParam String message,
            @RequestBody(required = false) List<String> history) {
        return cachedChatService.contextualChatWithCache(contextId, message, history);
    }
}
