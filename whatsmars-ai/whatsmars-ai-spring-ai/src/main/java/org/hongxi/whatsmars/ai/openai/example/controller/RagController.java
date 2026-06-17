package org.hongxi.whatsmars.ai.openai.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG (检索增强生成) 示例控制器
 * <p>
 * 演示如何使用向量数据库实现知识库问答
 * </p>
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai/rag")
public class RagController {

    private static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagController(ChatClient.Builder builder, EmbeddingModel embeddingModel) {
        this.chatClient = builder.build();
        // 使用内存向量存储（生产环境建议使用 Redis、Milvus、Chroma 等）
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        
        // 初始化示例数据
        initializeKnowledgeBase();
    }

    /**
     * 初始化知识库
     */
    private void initializeKnowledgeBase() {
        List<Document> documents = List.of(
                new Document("Spring Boot 是一个用于快速构建基于 Spring 框架的生产级应用程序的框架。" +
                        "它提供了自动配置、嵌入式服务器和开箱即用的功能。"),
                new Document("Apache Dubbo 是一款高性能、轻量级的开源 Java RPC 框架。" +
                        "它提供了三大核心能力：面向接口的远程方法调用、智能容错和负载均衡、服务自动注册和发现。"),
                new Document("Redis 是一个开源的内存数据结构存储系统，可用作数据库、缓存和消息中间件。" +
                        "它支持多种数据结构，如字符串、哈希、列表、集合等。"),
                new Document("Kafka 是一个分布式流处理平台，具有高吞吐量、可扩展性和容错性。" +
                        "它常用于构建实时数据管道和流式应用。"),
                new Document("Elasticsearch 是一个分布式搜索和分析引擎，基于 Lucene 构建。" +
                        "它提供了 RESTful API，支持全文搜索、结构化搜索和分析功能。")
        );

        vectorStore.add(documents);
        log.info("知识库初始化完成，共 {} 条文档", documents.size());
    }

    /**
     * 添加文档到知识库
     * <p>
     *     测试示例：
     *     Nacos 是一个易于构建 AI Agent 应用的动态服务发现、配置管理和 AI 智能体管理平台
     * </p>
     *
     * @param content 文档内容
     * @return 操作结果
     */
    @PostMapping("/document")
    public Map<String, Object> addDocument(@RequestParam String content) {
        log.info("添加文档: {}", content.substring(0, Math.min(50, content.length())));

        Document document = new Document(content);
        vectorStore.add(List.of(document));

        Map<String, Object> result = new HashMap<>();
        result.put("message", "文档添加成功");
        result.put("contentLength", content.length());
        return result;
    }

    /**
     * RAG 问答接口
     * <p>
     * 1. 根据用户问题检索相关文档
     * 2. 将检索结果作为上下文提供给 AI
     * 3. AI 基于上下文回答问题
     *
     * 测试示例：
     *     国内最流行的RPC框架是哪一款
     * </p>
     *
     * @param question 用户问题
     * @return AI 回答
     */
    @GetMapping("/ask")
    public Map<String, Object> askQuestion(@RequestParam String question) {
        log.info("RAG 问答 - 问题: {}", question);

        // 步骤 1: 检索相关文档（简化版本）
        List<Document> relevantDocs = vectorStore.similaritySearch(question);

        log.info("检索到 {} 条相关文档", relevantDocs.size());

        // 步骤 2: 构建上下文
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 步骤 3: 使用 RAG 模式提问
        String answer = chatClient.prompt()
                .system("你是一个技术专家助手。请基于提供的上下文信息回答用户的问题。如果上下文中没有相关信息，请明确说明。\n\n上下文：\n" + context)
                .user(question)
                .call()
                .content();

        log.info("AI 回答: {}", answer);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("answer", answer);
        result.put("relevantDocuments", relevantDocs.stream()
                .map(doc -> Map.of(
                        "content", doc.getText(),
                        "score", doc.getScore()
                ))
                .collect(Collectors.toList()));
        result.put("docCount", relevantDocs.size());

        return result;
    }

    /**
     * 清空知识库
     *
     * @return 操作结果
     */
    @DeleteMapping("/documents")
    public Map<String, String> clearDocuments() {
        log.info("清空知识库");
        // SimpleVectorStore 不支持直接清空，实际项目中需要实现自定义逻辑
        Map<String, String> result = new HashMap<>();
        result.put("message", "知识库清空功能需要根据具体 VectorStore 实现");
        return result;
    }
}
