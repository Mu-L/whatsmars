package org.hongxi.whatsmars.rocketmq.v5.quickstart;

import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.apis.producer.Transaction;
import org.apache.rocketmq.client.apis.producer.TransactionChecker;
import org.apache.rocketmq.client.apis.producer.TransactionResolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerTransactionMessageExample {
    private static final Logger log = LoggerFactory.getLogger(ProducerTransactionMessageExample.class);

    private ProducerTransactionMessageExample() {
    }

    public static void main(String[] args) throws ClientException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();

        String topic = "example-trans-topic";
        TransactionChecker checker = messageView -> {
            log.info("Received transactional message check, message={}", messageView);
            // Return the transaction resolution according to your business logic.
            return TransactionResolution.COMMIT;
        };
        // Get producer using singleton pattern.
        // For transaction producers, it is essential to set topics to ensure the reliability of the transaction
        // checker.
        final Producer producer = ProducerSingleton.getTransactionalInstance(checker, topic);
        final Transaction transaction = producer.beginTransaction();
        // Define your message body.
        byte[] body = "This is a transaction message for Apache RocketMQ".getBytes(StandardCharsets.UTF_8);
        String tag = "TagA";
        final Message message = provider.newMessageBuilder()
            // Set topic for the current message.
            .setTopic(topic)
            // Message secondary classifier of message besides topic.
            .setTag(tag)
            // Key(s) of the message, another way to mark message besides message id.
            .setKeys("565ef26f5727")
            .setBody(body)
            .build();
        try {
            final SendReceipt sendReceipt = producer.send(message, transaction);
            log.info("Send transaction message successfully, messageId={}", sendReceipt.getMessageId());
        } catch (Exception e) {
            log.error("Failed to send message", e);
            return;
        }
        // Commit the transaction.
        transaction.commit();
        // Or rollback the transaction.
        // transaction.rollback();

        // Close the producer when you don't need it anymore.
        // You could close it manually or add this into the JVM shutdown hook.
        // producer.close();
    }
}
