package org.hongxi.whatsmars.ai.openai.example.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 用户信息工具类
 * <p>
 * 使用 @Tool 注解定义 AI 可调用的用户信息查询函数
 * </p>
 *
 * @author hongxi
 */
@Component
public class UserTools {

    /**
     * 根据用户名获取用户的详细信息
     *
     * @param name 用户姓名
     * @return 用户详细信息
     */
    @Tool(description = "根据用户名获取用户的详细信息")
    public String getUserInfo(@ToolParam(description = "用户姓名") String name) {
        // 模拟用户数据（实际项目中可以从数据库查询）
        if ("张三".equals(name)) {
            return "姓名：张三，年龄：25岁，职业：软件工程师，爱好：编程和打篮球，邮箱：zhangsan@example.com";
        } else if ("李四".equals(name)) {
            return "姓名：李四，年龄：30岁，职业：产品经理，爱好：阅读和旅行，邮箱：lisi@example.com";
        } else if ("王五".equals(name)) {
            return "姓名：王五，年龄：28岁，职业：设计师，爱好：绘画和摄影，邮箱：wangwu@example.com";
        } else {
            return "未找到用户 " + name + " 的信息";
        }
    }
}
