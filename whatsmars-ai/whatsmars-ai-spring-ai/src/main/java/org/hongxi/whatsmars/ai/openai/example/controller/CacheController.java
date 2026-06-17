package org.hongxi.whatsmars.ai.openai.example.controller;

import org.hongxi.whatsmars.ai.openai.example.cache.CachedChatService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param question 用户问题
     * @return AI 回答和缓存状态
     */
    @GetMapping("/chat")
    public Map<String, Object> cachedChat(@RequestParam String question) {
        long startTime = System.currentTimeMillis();
        String answer = cachedChatService.chatWithCache(question);
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("answer", answer);
        result.put("responseTimeMs", duration);
        result.put("cached", duration < 100); // 小于 100ms 很可能是缓存命中
        
        return result;
    }

    /**
     * 带系统提示词的缓存聊天
     *
     * @param systemPrompt 系统提示词
     * @param question 用户问题
     * @return AI 回答
     */
    @PostMapping("/chat-with-system")
    public Map<String, Object> chatWithSystem(
            @RequestParam String systemPrompt,
            @RequestParam String question) {
        long startTime = System.currentTimeMillis();
        String answer = cachedChatService.chatWithSystemAndCache(systemPrompt, question);
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("systemPrompt", systemPrompt);
        result.put("question", question);
        result.put("answer", answer);
        result.put("responseTimeMs", duration);
        
        return result;
    }

    /**
     * RAG 问答（带缓存）
     *
     * @param question 用户问题
     * @param context 检索到的上下文
     * @return AI 回答
     */
    @PostMapping("/rag-chat")
    public Map<String, Object> ragChat(@RequestParam String question,
                                       @RequestParam String context) {
        long startTime = System.currentTimeMillis();
        String answer = cachedChatService.ragChatWithCache(question, context);
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("answer", answer);
        result.put("responseTimeMs", duration);
        result.put("cached", duration < 100);
        
        return result;
    }

    /**
     * 清除指定问题的缓存
     *
     * @param question 问题内容
     * @return 操作结果
     */
    @DeleteMapping("/evict")
    public Map<String, Object> evictCache(@RequestParam String question) {
        cachedChatService.evictQuestionCache(question);
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "已清除问题 \"" + question + "\" 的缓存");
        
        return result;
    }

    /**
     * 清除所有聊天缓存
     *
     * @return 操作结果
     */
    @DeleteMapping("/clear-all")
    public Map<String, Object> clearAllCache() {
        cachedChatService.clearAllChatCache();
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "已清除所有聊天缓存");
        
        return result;
    }

    /**
     * 缓存性能对比测试
     * <p>
     * 连续调用两次相同问题，对比响应时间
     * </p>
     *
     * @param question 测试问题
     * @return 性能对比结果
     */
    @GetMapping("/benchmark")
    public Map<String, Object> benchmark(@RequestParam String question) {
        Map<String, Object> result = new HashMap<>();
        
        // 第一次调用（缓存未命中）
        long start1 = System.currentTimeMillis();
        String answer1 = cachedChatService.chatWithCache(question);
        long duration1 = System.currentTimeMillis() - start1;
        
        // 第二次调用（缓存命中）
        long start2 = System.currentTimeMillis();
        String answer2 = cachedChatService.chatWithCache(question);
        long duration2 = System.currentTimeMillis() - start2;
        
        result.put("question", question);
        result.put("firstCall", Map.of(
                "duration", duration1 + "ms",
                "cached", false,
                "answer", answer1
        ));
        result.put("secondCall", Map.of(
                "duration", duration2 + "ms",
                "cached", true,
                "answer", answer2
        ));
        result.put("performanceGain", (duration1 - duration2) + "ms");
        result.put("speedupRatio", duration2 > 0 ? String.format("%.2fx", (double) duration1 / duration2) : "N/A");
        
        return result;
    }

    /**
     * 多轮对话缓存示例
     *
     * @param contextId 会话ID
     * @param question 当前问题
     * @param history 对话历史（JSON 数组）
     * @return AI 回答
     */
    @PostMapping("/contextual-chat")
    public Map<String, Object> contextualChat(
            @RequestParam String contextId,
            @RequestParam String question,
            @RequestBody(required = false) List<String> history) {
        long startTime = System.currentTimeMillis();
        String answer = cachedChatService.contextualChatWithCache(contextId, question, history);
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> result = new HashMap<>();
        result.put("contextId", contextId);
        result.put("question", question);
        result.put("answer", answer);
        result.put("responseTimeMs", duration);
        
        return result;
    }
}
