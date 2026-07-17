package org.hongxi.whatsmars.ai.langchain4j.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG（检索增强生成）配置
 * <p>
 * 配置向量存储和内容检索器，用于 RAG 知识库问答
 * </p>
 *
 * @author hongxi
 */
@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    /**
     * 内存向量存储
     * <p>
     * 生产环境可替换为 Milvus / Elasticsearch / PgVector 等持久化向量数据库
     * </p>
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * 内容检索器
     * <p>
     * 根据用户查询从向量存储中检索最相关的文档片段
     * </p>
     *
     * @param embeddingModel 嵌入模型（由 langchain4j-open-ai-spring-boot-starter 自动配置）
     * @param embeddingStore 向量存储
     * @return 内容检索器
     */
    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {
        log.info("初始化 ContentRetriever");
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.7)
                .build();
    }
}
