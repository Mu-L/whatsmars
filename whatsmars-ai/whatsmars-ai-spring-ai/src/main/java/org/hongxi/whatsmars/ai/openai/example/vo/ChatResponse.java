package org.hongxi.whatsmars.ai.openai.example.vo;

/**
 * AI 对话响应
 *
 * @param userMessage  用户消息
 * @param aiResponse   AI 回复
 * @author hongxi
 */
public record ChatResponse(String userMessage, String aiResponse) {
}
