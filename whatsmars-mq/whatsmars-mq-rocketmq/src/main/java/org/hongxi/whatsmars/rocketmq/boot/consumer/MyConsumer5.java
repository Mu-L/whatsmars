package org.hongxi.whatsmars.rocketmq.boot.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;

/**
 * 配置重试次数(本人修改点) reconsumeTimes
 */
@Slf4j
@RocketMQMessageListener(topic = "test-topic-4", consumerGroup = "my-consumer_test-topic-5",
    consumeMode = ConsumeMode.ORDERLY)
public class MyConsumer5 implements RocketMQListener<MessageExt> {
    @Override
    public void onMessage(MessageExt messageExt) {
        log.info("received message: " + messageExt);
        throw new RuntimeException("test retry consume");
    }
}