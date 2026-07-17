package org.hongxi.whatsmars.ai.controller;

import org.hongxi.whatsmars.ai.service.RagService;
import org.hongxi.whatsmars.ai.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * RAG（检索增强生成）控制器
 * <p>
 * 提供知识库文档摄入与 RAG 查询接口，演示完整的 RAG 流程：
 * 1. POST /ai/rag/ingest  — 摄入文档到向量数据库
 * 2. GET  /ai/rag/query   — 基于知识库的 RAG 问答
 * 3. DELETE /ai/rag/documents — 清除指定来源的文档
 * </p>
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai/rag")
public class RagController {

    private static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 摄入文档到向量数据库
     * <p>
     * 示例请求体：
     * <pre>
     * {
     *   "content": "Spring AI 是 Spring 生态中用于集成 AI 模型的框架...",
     *   "source": "spring-ai-docs"
     * }
     * </pre>
     *
     * @param request 包含 content（文本内容）和 source（来源标识）
     * @return 分块后存储的文档数量
     */
    @PostMapping("/ingest")
    public ResponseEntity<?> ingest(@RequestBody IngestRequest request) {
        String content = request.content();
        String source = request.source() != null ? request.source() : "default";
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body("content 不能为空");
        }
        log.info("RAG 文档摄入请求，source={}, contentLength={}", source, content.length());
        int chunks = ragService.ingest(content, source);
        return ResponseEntity.ok(new IngestResponse(source, chunks, "文档摄入成功"));
    }

    /**
     * RAG 查询（流式）：基于知识库检索并增强 LLM 回答
     *
     * @param question 用户问题
     * @param topK     检索文档数量
     * @return 流式 LLM 回答
     */
    @GetMapping("/query")
    public ResponseEntity<Flux<String>> queryStream(@RequestParam String question,
                                        @RequestParam(required = false, defaultValue = "3") int topK) {
        log.info("RAG 流式查询请求，question={}, topK={}", question, topK);
        Flux<String> stream = ragService.queryStream(question, topK);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/event-stream;charset=UTF-8"))
                .header("Cache-Control", "no-cache")
                .body(stream);
    }

    /**
     * 删除指定来源的所有文档
     *
     * @param source 来源标识
     */
    @DeleteMapping("/documents")
    public ResponseEntity<String> deleteDocuments(String source) {
        log.info("删除文档请求，source={}", source);
        ragService.deleteBySource(source);
        return ResponseEntity.ok("文档删除成功");
    }
}
