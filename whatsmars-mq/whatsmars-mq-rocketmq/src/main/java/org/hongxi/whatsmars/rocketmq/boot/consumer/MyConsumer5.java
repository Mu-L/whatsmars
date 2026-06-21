package org.hongxi.whatsmars.rocketmq.boot.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RocketMQ 消息消费者实现类
 * <p>
 * 用于监听并处理来自 test-topic-5 主题的消息（事务消息）
 */
@Service
@RocketMQMessageListener(topic = "test-topic-5", consumerGroup = "my-consumer_test-topic-5")
public class MyConsumer5 implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(MyConsumer5.class);

    @Override
    public void onMessage(String message) {
        log.info("received message: {}", message);
    }
}