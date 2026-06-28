package org.hongxi.whatsmars.ai.openai.example.vo;

/**
 * 通用清空操作结果
 *
 * @author hongxi
 */
public record ClearResult(String message, String hint) {

    public ClearResult(String message) {
        this(message, null);
    }
}
