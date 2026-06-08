package org.hongxi.whatsmars.rocketmq.boot.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.hongxi.whatsmars.rocketmq.boot.OrderPaidEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigDecimal;

/**
 * RocketMQ 消息生产者应用
 * <p>
 * 演示了多种消息发送方式，包括同步发送、异步发送、单向发送、延迟消息和顺序消息等
 */
@Slf4j
@SpringBootApplication
public class ProducerApplication implements CommandLineRunner {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public static void main(String[] args){
        SpringApplication.run(ProducerApplication.class, args);
    }
    
    /**
     * 应用启动后执行的消息发送逻辑
     * <p>
     * 演示了多种 RocketMQ 消息发送方式：
     * 1. 批量发送普通消息
     * 2. 使用 Spring Message 发送消息
     * 3. 同步发送简单消息
     * 4. 发送延迟消息
     * 5. 单向发送消息（不关心发送结果）
     * 6. 异步发送消息（带回调）
     * 7. 发送对象类型消息
     * 8. 发送顺序消息
     *
     * @param args 命令行参数
     * @throws Exception 执行异常
     */
    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 5; i++) {
            try {
                rocketMQTemplate.convertAndSend("test-topic-1", "Hello, World!");
                System.out.println("Send OK!");
            } catch (Exception e) {
                log.error("Send Failed", e);
            }
        }
        rocketMQTemplate.send("test-topic-1", MessageBuilder.withPayload("Hello, World! I'm from spring message").build());
        rocketMQTemplate.syncSend("test-topic-1", "Hello, World! I'm from simple message");
        rocketMQTemplate.syncSendDelayTimeSeconds("test-topic-1", "I'm delayed message", 5);
        rocketMQTemplate.sendOneWay("test-topic-1", MessageBuilder.withPayload("I'm one way message").build());
        rocketMQTemplate.asyncSend("test-topic-1", MessageBuilder.withPayload("I'm async message").build(), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("async: {}", sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.error(e.getMessage(), e);
            }
        });
        rocketMQTemplate.convertAndSend("test-topic-2", new OrderPaidEvent("T_001", new BigDecimal("88.00")));
        rocketMQTemplate.syncSendOrderly("test-topic-3", "I'm order message", "1234");
        log.info("send finished!");
    }

}