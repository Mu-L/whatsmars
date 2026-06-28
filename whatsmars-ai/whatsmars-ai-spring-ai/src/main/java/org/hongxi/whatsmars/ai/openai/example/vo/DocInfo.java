package org.hongxi.whatsmars.ai.openai.example.vo;

/**
 * Redis RAG 按分类检索 - 单个文档信息
 *
 * @author hongxi
 */
public record DocInfo(String content, String source, double score) {
}
