package org.hongxi.whatsmars.ai.openai.example.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 搜索工具类
 * <p>
 * 模拟搜索引擎功能，提供知识查询能力
 * </p>
 *
 * @author hongxi
 */
@Component
public class SearchTools {

    private static final Map<String, String> KNOWLEDGE_BASE = new ConcurrentHashMap<>();

    static {
        // 初始化知识库
        KNOWLEDGE_BASE.put("Spring Boot",
            "Spring Boot 是一个用于快速构建基于 Spring 框架的生产级应用程序的框架。" +
            "它简化了 Spring 应用的初始搭建和开发过程，提供了自动配置、嵌入式服务器等功能。");
        
        KNOWLEDGE_BASE.put("Apache Dubbo",
            "Apache Dubbo 是一款高性能、轻量级的开源 Java RPC 框架。" +
            "它提供了三大核心能力：面向接口的远程方法调用、智能容错和负载均衡、服务自动注册和发现。");
        
        KNOWLEDGE_BASE.put("Redis",
            "Redis 是一个开源的内存数据结构存储系统，可用作数据库、缓存和消息中间件。" +
            "它支持多种类型的数据结构，如字符串、哈希、列表、集合、有序集合等。");
        
        KNOWLEDGE_BASE.put("Kafka",
            "Apache Kafka 是一个分布式流处理平台，具有高吞吐量、低延迟的特点。" +
            "常用于构建实时数据管道和流式应用，支持消息持久化和多订阅者模式。");
        
        KNOWLEDGE_BASE.put("Nacos",
            "Nacos 是一个易于构建云原生应用的动态服务发现、配置管理和服务管理平台。" +
            "它支持服务注册与发现、配置管理、服务健康监测等功能。");
    }

    /**
     * 搜索指定主题的信息
     *
     * @param query 搜索关键词
     * @return 搜索结果描述
     */
    @Tool(description = "搜索指定主题的相关信息，返回简要介绍")
    public String search(@ToolParam(description = "搜索关键词，例如：Spring Boot、Redis、Kafka") String query) {
        String lowerQuery = query.toLowerCase();
        
        // 模糊匹配知识库
        for (Map.Entry<String, String> entry : KNOWLEDGE_BASE.entrySet()) {
            if (lowerQuery.contains(entry.getKey()) || entry.getKey().contains(lowerQuery)) {
                return entry.getValue();
            }
        }
        
        return "未找到关于 \"" + query + "\" 的相关信息";
    }

    /**
     * 获取最新的技术资讯
     *
     * @param topic 技术主题
     * @return 最新资讯摘要
     */
    @Tool(description = "获取指定技术主题的最新资讯或发展趋势")
    public String getLatestNews(@ToolParam(description = "技术主题，例如：人工智能、微服务、云计算") String topic) {
        return switch (topic.toLowerCase()) {
            case "人工智能", "ai" -> "人工智能领域正在快速发展，大语言模型、多模态AI、Agent技术成为热点。" +
                    "企业正在探索AI在实际业务场景中的应用，如智能客服、代码生成、数据分析等。";
            case "微服务" -> "微服务架构继续演进，Service Mesh、Serverless、云原生等技术成为主流。" +
                    "越来越多的企业采用容器化部署和Kubernetes编排来管理微服务。";
            case "云计算" -> "云计算进入多云和混合云时代，边缘计算、云原生安全、FinOps等概念受到关注。" +
                    "各大云厂商持续推出新的托管服务和AI能力。";
            default -> "暂无 \"" + topic + "\" 相关的最新资讯";
        };
    }
}
