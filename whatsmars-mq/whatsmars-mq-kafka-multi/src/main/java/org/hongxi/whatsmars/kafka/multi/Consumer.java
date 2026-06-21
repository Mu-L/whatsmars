package org.hongxi.whatsmars.kafka.multi;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka 消息消费者
 * <p>
 * 监听多个 Kafka 集群的消息，演示多集群消费场景
 */
@Component
public class Consumer {

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    /**
     * 处理来自第一个 Kafka 集群的消息
     *
     * @param message 接收到的消息内容
     */
    @KafkaListener(topics = "kafkaTest", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(String message) {
        log.info("kafkaTest received message: {}", message);
    }

    /**
     * 处理来自第二个 Kafka 集群的消息
     *
     * @param message 接收到的消息内容
     */
    @KafkaListener(topics = "kafkaTest2", containerFactory = "kafkaListenerContainerFactory2")
    public void onMessage2(String message) {
        log.info("kafkaTest2 received message: {}", message);
    }
}
