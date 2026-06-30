package org.hongxi.whatsmars.ai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 结构化输出控制器
 * <p>
 * 演示如何让 AI 返回结构化的 JSON 数据，并自动转换为 Java 对象
 * </p>
 *
 * @author hongxi
 */
@RestController
@RequestMapping("/ai/structured")
public class StructuredOutputController {

    private static final Logger log = LoggerFactory.getLogger(StructuredOutputController.class);

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 提取用户信息为结构化数据
     * <p>
     * 测试示例: "我叫张三，今年25岁，是一名软件工程师，喜欢编程和打篮球，邮箱是zhangsan@example.com"
     * </p>
     *
     * @param text 包含用户信息的自然语言文本
     * @return 结构化的用户信息
     */
    @PostMapping("/extract-user")
    public Object extractUserInfo(@RequestParam String text) {
        log.info("提取用户信息: {}", text);

        record UserInfo(String name, Integer age, String email, List<String> hobbies, String occupation) {}

        return chatClient.prompt()
                .system("""
                        你是一个信息提取助手。请从用户的描述中提取个人信息，并以 JSON 格式返回。
                        
                        要求：
                        - name: 姓名（字符串）
                        - age: 年龄（整数）
                        - email: 邮箱（字符串）
                        - hobbies: 爱好（字符串数组）
                        - occupation: 职业（字符串）
                        
                        如果某些信息不存在，用 null 或空数组表示。
                        """)
                .user(text)
                .call()
                .entity(UserInfo.class);
    }

    /**
     * 生成产品评论摘要
     * <p>
     *     测试示例：
     *     小米17 Pro是一款极具个性的6.3英寸小屏旗舰，搭载骁龙8至尊版与徕卡全焦段影像，拍照出色。
     *     6300mAh大电池续航有保障，背屏设计充满可玩性，但待机功耗偏高且实用性有限，适合追求手感与影像的用户。
     * </p>
     *
     * @param review 产品评论
     * @return 结构化的评论摘要
     */
    @PostMapping("/review-summary")
    public Object reviewSummary(@RequestParam String review) {
        log.info("分析产品评论: {}", review);

        record ReviewSummary(String sentiment, Integer rating, List<String> pros, List<String> cons, String summary) {}

        return chatClient.prompt()
                .system("""
                        你是一个产品评论分析助手。请分析用户评论并提取关键信息。
                        
                        返回格式：
                        - sentiment: 情感倾向（positive/negative/neutral）
                        - rating: 评分（1-5分）
                        - pros: 优点列表
                        - cons: 缺点列表
                        - summary: 一句话总结
                        """)
                .user(review)
                .call()
                .entity(ReviewSummary.class);
    }
}
