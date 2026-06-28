package org.hongxi.whatsmars.ai.openai.example.vo;

import java.util.List;

/**
 * Redis RAG 按分类检索结果
 *
 * @author hongxi
 */
public record SearchByCategoryResult(String category, String query, List<DocInfo> documents, int count) {
}
