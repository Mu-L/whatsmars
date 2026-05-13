package org.hongxi.whatsmars.ai.langchain4j.sample;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService // 标记为 AI 服务，会被自动扫描并注册为 Spring Bean
public interface Assistant {

    // 设定系统提示词，规定 AI 的人设
    @SystemMessage("你是一个专业的 Java 技术专家，回答要简洁、准确。")
    String chat(String userMessage);
}