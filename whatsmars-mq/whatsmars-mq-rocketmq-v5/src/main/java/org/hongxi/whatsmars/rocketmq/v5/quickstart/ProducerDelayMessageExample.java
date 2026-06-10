package org.hongxi.whatsmars.rocketmq.v5.quickstart;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerDelayMessageExample {
    private static final Logger log = LoggerFactory.getLogger(ProducerDelayMessageExample.class);

    private ProducerDelayMessageExample() {
    }

    public static void main(String[] args) throws ClientException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();

        String topic = "example-delay-topic";
        final Producer producer = ProducerSingleton.getInstance(topic);
        // Define your message body.
        byte[] body = "This is a delay message for Apache RocketMQ".getBytes(StandardCharsets.UTF_8);
        String tag = "TagA";
        Duration messageDelayTime = Duration.ofSeconds(10);
        final Message message = provider.newMessageBuilder()
            // Set topic for the current message.
            .setTopic(topic)
            // Message secondary classifier of message besides topic.
            .setTag(tag)
            // Key(s) of the message, another way to mark message besides message id.
            .setKeys("3ee439f945d7")
            // Set expected delivery timestamp of message.
            .setDeliveryTimestamp(System.currentTimeMillis() + messageDelayTime.toMillis())
            .setBody(body)
            .build();
        try {
            final SendReceipt sendReceipt = producer.send(message);
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
        // Close the producer when you don't need it anymore.
        // You could close it manually or add this into the JVM shutdown hook.
        // producer.close();
    }
}