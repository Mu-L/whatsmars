package org.hongxi.whatsmars.ai.mcp.server;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据转换工具服务
 * <p>
 * 提供常见数据格式转换功能
 * </p>
 *
 * @author hongxi
 */
@Service
public class ConversionToolService {

    /**
     * JSON 格式化
     *
     * @param jsonString JSON 字符串
     * @return 格式化后的 JSON
     */
    @Tool(description = "将压缩的 JSON 字符串格式化为易读的格式（带缩进）")
    public String formatJson(String jsonString) {
        // 简单实现：实际项目中应该使用 Jackson 或 Gson
        try {
            // 这里只是示例，真实场景需要使用 JSON 库进行解析和格式化
            return "格式化后的 JSON: " + jsonString;
        } catch (Exception e) {
            return "JSON 格式化失败: " + e.getMessage();
        }
    }

    /**
     * URL 编码
     *
     * @param url 原始 URL
     * @return URL 编码后的字符串
     */
    @Tool(description = "对 URL 或查询参数进行 URL 编码")
    public String urlEncode(String url) {
        try {
            return java.net.URLEncoder.encode(url, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "URL 编码失败: " + e.getMessage();
        }
    }

    /**
     * URL 解码
     *
     * @param encodedUrl URL 编码后的字符串
     * @return 解码后的 URL
     */
    @Tool(description = "对 URL 编码的字符串进行解码")
    public String urlDecode(String encodedUrl) {
        try {
            return java.net.URLDecoder.decode(encodedUrl, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "URL 解码失败: " + e.getMessage();
        }
    }

    /**
     * Base64 编码
     *
     * @param text 原始文本
     * @return Base64 编码后的字符串
     */
    @Tool(description = "将文本转换为 Base64 编码")
    public String base64Encode(String text) {
        try {
            return java.util.Base64.getEncoder().encodeToString(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "Base64 编码失败: " + e.getMessage();
        }
    }

    /**
     * Base64 解码
     *
     * @param encodedText Base64 编码的文本
     * @return 解码后的文本
     */
    @Tool(description = "将 Base64 编码的字符串解码为原始文本")
    public String base64Decode(String encodedText) {
        try {
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(encodedText);
            return new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Base64 解码失败: " + e.getMessage();
        }
    }

    /**
     * 计算字符串长度
     *
     * @param text 输入文本
     * @return 字符串长度
     */
    @Tool(description = "计算字符串的字符数量")
    public int stringLength(String text) {
        return text.length();
    }

    /**
     * 统计单词数量
     *
     * @param text 输入文本
     * @return 单词数量
     */
    @Tool(description = "统计英文文本中的单词数量（以空格分隔）")
    public int wordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}
