package org.hongxi.whatsmars.ai.openai.example.vo;

/**
 * Agent 决策演示 - 多个示例的对比结果
 *
 * @author hongxi
 */
public record DemoResult(ChatResponse weatherExample, ChatResponse searchExample,
                         ChatResponse calculationExample, ChatResponse complexExample) {
}
