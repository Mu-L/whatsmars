package org.hongxi.whatsmars.rocketmq.boot.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.hongxi.whatsmars.rocketmq.boot.OrderPaidEvent;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 消息消费者实现类
 * <p>
 * 用于监听并处理来自 test-topic-2 主题的订单支付事件消息
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = "test-topic-2", consumerGroup = "my-consumer_test-topic-2")
public class MyConsumer2 implements RocketMQListener<OrderPaidEvent> {
    /**
     * 处理接收到的订单支付事件消息
     * <p>
     * 当接收到订单支付事件时，记录事件内容到日志
     *
     * @param orderPaidEvent 接收到的订单支付事件对象
     */
    @Override
    public void onMessage(OrderPaidEvent orderPaidEvent) {
        log.info("received orderPaidEvent: {}", orderPaidEvent);
    }
}