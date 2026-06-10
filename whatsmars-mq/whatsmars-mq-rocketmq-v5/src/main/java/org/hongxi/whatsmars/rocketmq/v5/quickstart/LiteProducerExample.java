package org.hongxi.whatsmars.rocketmq.v5.quickstart;

import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.java.exception.LiteTopicQuotaExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiteProducerExample {
    static final Logger log = LoggerFactory.getLogger(LiteProducerExample.class);

    private LiteProducerExample() {
    }

    public static void main(String[] args) throws ClientException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();

        String topic = "example-lite-topic";
        final Producer producer = ProducerSingleton.getInstance(topic);
        // Define your message body.
        byte[] body = "This is a lite message for Apache RocketMQ".getBytes(StandardCharsets.UTF_8);
        final Message message = provider.newMessageBuilder()
            // Set topic for the current message.
            .setTopic(topic)
            // Key(s) of the message, another way to mark message besides message id.
            .setKeys("3ee439f945d7")
            // Set your lite topic
            .setLiteTopic("lite-topic-1")
            .setBody(body)
            .build();
        try {
            final SendReceipt sendReceipt = producer.send(message);
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        } catch (LiteTopicQuotaExceededException e) {
            // Lite topic quota exceeded.
            // Evaluate and increase the lite topic resource limit.
            log.error("Lite topic quota exceeded", e);
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
        // Close the producer when you don't need it anymore.
        // You could close it manually or add this into the JVM shutdown hook.
        // producer.close();
    }
}
