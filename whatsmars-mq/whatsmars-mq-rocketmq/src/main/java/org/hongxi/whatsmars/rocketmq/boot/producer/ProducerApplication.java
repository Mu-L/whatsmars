package org.hongxi.whatsmars.rocketmq.boot.producer;

import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.hongxi.whatsmars.rocketmq.boot.OrderPaidEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RocketMQ 消息生产者应用
 * <p>
 * 演示了多种消息发送方式，包括同步发送、异步发送、单向发送、延迟消息和顺序消息等
 */
@SpringBootApplication
public class ProducerApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProducerApplication.class);
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public static void main(String[] args){
        SpringApplication.run(ProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 同步发送，普通消息、顺序消息、延时消息
        rocketMQTemplate.syncSend("test-topic-1", "I'm normal message");
        rocketMQTemplate.syncSendOrderly("test-topic-3", "I'm order message", "1234");
        rocketMQTemplate.syncSendDelayTimeSeconds("test-topic-4", "I'm delayed message", 5);

        // use MessageBuilder
        Message<String> message = MessageBuilder.withPayload("I'm normal message").setHeader("orderId", "1").build();
        rocketMQTemplate.syncSend("test-topic-1", message);

        // 单向发送
        rocketMQTemplate.sendOneWay("test-topic-1", "I'm one way message");

        // 异步发送
        rocketMQTemplate.asyncSend("test-topic-1", "I'm async message", new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("async: {}", sendResult);
            }

            @Override
            public void onException(Throwable e) {
                log.error(e.getMessage(), e);
            }
        });

        // spring message (send, convertAndSend 是 spring-messaging 抽象的方法)
        rocketMQTemplate.send("test-topic-1", MessageBuilder.withPayload("I'm spring message").build());
        rocketMQTemplate.convertAndSend("test-topic-1", "I'm spring message");
        rocketMQTemplate.convertAndSend("test-topic-2", new OrderPaidEvent("T_001", new BigDecimal("88.00")));

        String orderId = "3519411001923440";
        Message<String> transactionMessage = MessageBuilder.withPayload("I'm transactional message")
                .setHeader("orderId", orderId).build();
        // 第三个参数可传与本地事务有关的数据，如orderId
        TransactionSendResult transactionSendResult = rocketMQTemplate.sendMessageInTransaction(
                "test-topic-5", transactionMessage, orderId);
        log.info("transactionSend to topic {} sendResult={}", "test-topic-5", transactionSendResult);

        log.info("send finished!");
    }

}