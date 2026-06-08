package org.hongxi.whatsmars.rocketmq.boot.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 消息消费者实现类
 * <p>
 * 用于监听并处理来自 test-topic-4 主题的消息
 * 配置了独立的 NameServer 地址和实例名称，适用于多集群场景
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "test-topic-4", consumerGroup = "my-consumer_test-topic-4",
nameServer = "${bigdata.name-server}", instanceName = "BIG_DATA_CLUSTER")
public class MyConsumer4 implements RocketMQListener<String> {
    /**
     * 处理接收到的消息
     * <p>
     * 当接收到消息时，记录消息内容到日志
     *
     * @param message 接收到的字符串类型消息内容
     */
    @Override
    public void onMessage(String message) {
        log.info("received message: {}", message);
    }
}