package org.hongxi.whatsmars.rocketmq.delay;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

public class Producer {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("delay_message_producer_group");
        producer.setNamesrvAddr("127.0.0.1:9876");
        producer.start();

        for (int i = 0; i < 10; i++) {
            Message message = new Message("TestTopic2", ("Hello RocketMQ " + i).getBytes(StandardCharsets.UTF_8));
            // 设置绝对投递时间戳（毫秒级精度）
            long deliverTime = System.currentTimeMillis() + 5_000;
            message.setDeliverTimeMs(deliverTime);
            SendResult sendResult = producer.send(message);
            System.out.println(sendResult);
        }

        producer.shutdown();
    }
}
