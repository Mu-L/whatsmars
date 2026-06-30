package org.hongxi.whatsmars.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 计算工具类
 * <p>
 * 提供数学计算能力，供 Agent 在需要时调用
 * </p>
 *
 * @author hongxi
 */
@Component
public class CalculatorTools {

    /**
     * 执行加法运算
     *
     * @param a 第一个数
     * @param b 第二个数
     * @return 两数之和
     */
    @Tool(description = "计算两个数的和")
    public double add(@ToolParam(description = "第一个数") double a, 
                      @ToolParam(description = "第二个数") double b) {
        return a + b;
    }

    /**
     * 执行减法运算
     *
     * @param a 被减数
     * @param b 减数
     * @return 两数之差
     */
    @Tool(description = "计算两个数的差")
    public double subtract(@ToolParam(description = "被减数") double a, 
                           @ToolParam(description = "减数") double b) {
        return a - b;
    }

    /**
     * 执行乘法运算
     *
     * @param a 第一个数
     * @param b 第二个数
     * @return 两数之积
     */
    @Tool(description = "计算两个数的乘积")
    public double multiply(@ToolParam(description = "第一个数") double a, 
                           @ToolParam(description = "第二个数") double b) {
        return a * b;
    }

    /**
     * 执行除法运算
     *
     * @param a 被除数
     * @param b 除数
     * @return 两数之商
     */
    @Tool(description = "计算两个数的商")
    public double divide(@ToolParam(description = "被除数") double a, 
                         @ToolParam(description = "除数") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为零");
        }
        return a / b;
    }

    /**
     * 计算百分比
     *
     * @param value 数值
     * @param percentage 百分比
     * @return 计算结果
     */
    @Tool(description = "计算某个数值的百分比")
    public double calculatePercentage(@ToolParam(description = "数值") double value, 
                                      @ToolParam(description = "百分比（0-100）") double percentage) {
        return value * (percentage / 100.0);
    }

    /**
     * 计算平均值
     *
     * @param numbers 数字列表（逗号分隔）
     * @return 平均值
     */
    @Tool(description = "计算一组数字的平均值")
    public double average(@ToolParam(description = "逗号分隔的数字列表，例如：10,20,30,40") String numbers) {
        try {
            String[] parts = numbers.split(",");
            double sum = 0;
            for (String part : parts) {
                sum += Double.parseDouble(part.trim());
            }
            return sum / parts.length;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的数字格式: " + numbers);
        }
    }
}
