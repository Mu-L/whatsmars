package org.hongxi.whatsmars.rocketmq.boot.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RocketMQ 消息消费者实现类
 * <p>
 * 用于监听并处理来自 test-topic-1 主题的消息
 */
@Service
@RocketMQMessageListener(topic = "test-topic-1", consumerGroup = "my-consumer_test-topic-1")
public class MyConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(MyConsumer.class);

    @Override
    public void onMessage(String message) {
        log.info("received message: {}", message);
    }
}