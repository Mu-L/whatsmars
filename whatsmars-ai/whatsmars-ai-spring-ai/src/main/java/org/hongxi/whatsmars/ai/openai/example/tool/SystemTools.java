package org.hongxi.whatsmars.ai.openai.example.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 通用系统工具类
 * <p>
 * 提供字符串处理等通用功能，既可用于内部 AI Tool Calling，
 * 也可通过 MCP 协议对外暴露给 MCP Client 调用。
 * </p>
 * <p>
 * 注意：数学运算功能请参见 {@link CalculatorTools}
 * </p>
 *
 * @author hongxi
 */
@Component
public class SystemTools {

    /**
     * 将文本转换为大写
     *
     * @param text 输入文本
     * @return 大写文本
     */
    @Tool(description = "将英文文本转换为大写形式")
    public String toUpperCase(@ToolParam(description = "要转换的英文文本") String text) {
        return text.toUpperCase();
    }

    /**
     * 将文本转换为小写
     *
     * @param text 输入文本
     * @return 小写文本
     */
    @Tool(description = "将英文文本转换为小写形式")
    public String toLowerCase(@ToolParam(description = "要转换的英文文本") String text) {
        return text.toLowerCase();
    }

    /**
     * 反转字符串
     *
     * @param text 输入文本
     * @return 反转后的文本
     */
    @Tool(description = "反转字符串中的字符顺序")
    public String reverseString(@ToolParam(description = "要反转的字符串") String text) {
        return new StringBuilder(text).reverse().toString();
    }
}
