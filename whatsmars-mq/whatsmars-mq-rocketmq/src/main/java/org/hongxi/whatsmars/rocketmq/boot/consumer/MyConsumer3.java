package org.hongxi.whatsmars.rocketmq.boot.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 消息消费者实现类
 * <p>
 * 用于监听并处理来自 test-topic-3 主题的消息，采用顺序消费模式
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "test-topic-3", consumerGroup = "my-consumer_test-topic-3",
    consumeMode = ConsumeMode.ORDERLY)
public class MyConsumer3 implements RocketMQListener<String> {
    /**
     * 处理接收到的消息（顺序消费）
     * <p>
     * 当接收到消息时，按照顺序处理并记录消息内容到日志
     *
     * @param message 接收到的字符串类型消息内容
     */
    @Override
    public void onMessage(String message) {
        log.info("received message: {}", message);
    }
}