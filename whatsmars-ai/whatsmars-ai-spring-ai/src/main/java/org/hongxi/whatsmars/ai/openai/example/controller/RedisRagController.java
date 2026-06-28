package org.hongxi.whatsmars.ai.openai.example.controller;

import org.hongxi.whatsmars.ai.openai.example.vo.ChatResponse;
import org.hongxi.whatsmars.ai.openai.example.vo.DocInfo;
import org.hongxi.whatsmars.ai.openai.example.vo.DocumentAddResult;
import org.hongxi.whatsmars.ai.openai.example.vo.ClearResult;
import org.hongxi.whatsmars.ai.openai.example.vo.SearchByCategoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Redis 向量存储 RAG 示例控制器
 * <p>
 * 演示如何使用 Redis 作为向量数据库实现知识库问答
 * </p>
 *
 * 需要先启动 Redis Stack (docker run -d --name redis-stack -p 6379:6379 redis/redis-stack:latest)
 *
 * @author hongxi
 */
// @RestController
@RequestMapping("/ai/rag-redis")
public class RedisRagController {

    private static final Logger log = LoggerFactory.getLogger(RedisRagController.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RedisRagController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        
        // 初始化示例数据
        initializeKnowledgeBase();
    }

    /**
     * 初始化知识库
     */
    private void initializeKnowledgeBase() {
        List<Document> documents = List.of(
                new Document("Spring Boot 是一个用于快速构建基于 Spring 框架的生产级应用程序的框架。" +
                        "它提供了自动配置、嵌入式服务器和开箱即用的功能。",
                        Map.of("source", "spring-boot", "category", "framework")),
                new Document("Apache Dubbo 是一款高性能、轻量级的开源 Java RPC 框架。" +
                        "它提供了三大核心能力：面向接口的远程方法调用、智能容错和负载均衡、服务自动注册和发现。",
                        Map.of("source", "dubbo", "category", "rpc")),
                new Document("Redis 是一个开源的内存数据结构存储系统，可用作数据库、缓存和消息中间件。" +
                        "它支持多种数据结构，如字符串、哈希、列表、集合等。",
                        Map.of("source", "redis", "category", "database")),
                new Document("Kafka 是一个分布式流处理平台，具有高吞吐量、可扩展性和容错性。" +
                        "它常用于构建实时数据管道和流式应用。",
                        Map.of("source", "kafka", "category", "messaging")),
                new Document("Elasticsearch 是一个分布式搜索和分析引擎，基于 Lucene 构建。" +
                        "它提供了 RESTful API，支持全文搜索、结构化搜索和分析功能。",
                        Map.of("source", "elasticsearch", "category", "search"))
        );

        vectorStore.add(documents);
        log.info("Redis 知识库初始化完成，共 {} 条文档", documents.size());
    }

    /**
     * 添加文档到 Redis 向量库
     *
     * @param content  文档内容
     * @param source   来源标识
     * @param category 分类
     * @return 操作结果
     */
    @PostMapping("/document")
    public DocumentAddResult addDocument(@RequestParam String content,
                                            @RequestParam(required = false) String source,
                                            @RequestParam(required = false) String category) {
        log.info("添加文档: {}", content.substring(0, Math.min(50, content.length())));

        Map<String, Object> metadata = new HashMap<>();
        if (source != null) {
            metadata.put("source", source);
        }
        if (category != null) {
            metadata.put("category", category);
        }

        Document document = new Document(content, metadata);
        vectorStore.add(List.of(document));

        return new DocumentAddResult("文档添加成功", content.length());
    }

    /**
     * RAG 问答接口（基于 Redis 向量检索）
     * <p>
     * 1. 根据用户问题在 Redis 中检索相关文档
     * 2. 将检索结果作为上下文提供给 AI
     * 3. AI 基于上下文回答问题
     * </p>
     *
     * @param message 用户问题
     * @param topK     返回最相关的 K 个文档（默认 3）
     * @return AI 回答
     */
    @GetMapping("/ask")
    public ChatResponse askQuestion(@RequestParam String message,
                                            @RequestParam(defaultValue = "3") int topK) {
        log.info("Redis RAG 问答 - 问题: {}", message);

        // 步骤 1: 从 Redis 检索相关文档
        SearchRequest searchRequest = SearchRequest.builder()
                .query(message)
                .topK(topK)
                .build();
        List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);

        log.info("从 Redis 检索到 {} 条相关文档", relevantDocs.size());

        // 步骤 2: 构建上下文
        String context = relevantDocs.stream()
                .map(doc -> String.format("[%s] %s", 
                        doc.getMetadata().getOrDefault("source", "unknown"),
                        doc.getText()))
                .collect(Collectors.joining("\n\n"));

        // 步骤 3: 使用 RAG 模式提问
        String answer = chatClient.prompt()
                .system("你是一个技术专家助手。请基于提供的上下文信息回答用户的问题。\n" +
                        "如果上下文中没有相关信息，请明确说明。\n\n" +
                        "上下文：\n" + context)
                .user(message)
                .call()
                .content();

        log.info("AI 回答: {}", answer);
        return new ChatResponse(message, answer);
    }

    /**
     * 按分类检索文档
     *
     * @param category 分类
     * @param query    查询文本
     * @param topK     返回数量
     * @return 相关文档
     */
    @GetMapping("/search-by-category")
    public SearchByCategoryResult searchByCategory(@RequestParam String category,
                                                  @RequestParam String query,
                                                  @RequestParam(defaultValue = "5") int topK) {
        log.info("按分类检索 - 分类: {}, 查询: {}", category, query);

        // 注意：Spring AI Redis Vector Store 目前不支持元数据过滤
        // 这里先检索所有文档，然后在代码层面过滤
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK * 3)
                .build();
        List<Document> allDocs = vectorStore.similaritySearch(searchRequest);
        
        List<Document> filteredDocs = allDocs.stream()
                .filter(doc -> category.equals(doc.getMetadata().get("category")))
                .limit(topK)
                .toList();

        return new SearchByCategoryResult(category, query,
                filteredDocs.stream()
                        .map(doc -> new DocInfo(
                                doc.getText(),
                                String.valueOf(doc.getMetadata().get("source")),
                                doc.getScore()
                        ))
                        .toList(),
                filteredDocs.size());
    }

    /**
     * 清空 Redis 中的向量数据
     *
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public ClearResult clearVectorStore() {
        log.info("清空 Redis 向量存储");
        
        // 注意：具体清空方式取决于 Redis Vector Store 的实现
        // 可能需要删除特定的 key 或使用 FLUSHDB
        return new ClearResult(
                "请使用 Redis CLI 执行: DEL <vector-index-key>",
                "查看 application.yml 中的 spring.ai.vectorstore.redis.index-name 配置"
        );
    }
}
