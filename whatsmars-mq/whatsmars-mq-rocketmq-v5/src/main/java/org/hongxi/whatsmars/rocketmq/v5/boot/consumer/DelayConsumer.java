package org.hongxi.whatsmars.rocketmq.v5.boot.consumer;

import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.core.RocketMQListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RocketMQ 延迟消息消费者实现类
 */
@Service
@RocketMQMessageListener(topic = "demo-delay-topic", consumerGroup = "my-consumer_demo-delay-topic")
public class DelayConsumer implements RocketMQListener {

    private static final Logger log = LoggerFactory.getLogger(DelayConsumer.class);

    @Override
    public ConsumeResult consume(MessageView messageView) {
        log.info("received message: {}", messageView);
        return ConsumeResult.SUCCESS;
    }
}