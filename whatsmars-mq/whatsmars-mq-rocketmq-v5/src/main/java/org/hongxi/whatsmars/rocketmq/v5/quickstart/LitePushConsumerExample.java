package org.hongxi.whatsmars.rocketmq.v5.quickstart;

import java.io.IOException;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.LitePushConsumer;
import org.apache.rocketmq.client.java.exception.LiteSubscriptionQuotaExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LitePushConsumerExample {
    private static final Logger log = LoggerFactory.getLogger(LitePushConsumerExample.class);

    private LitePushConsumerExample() {
    }

    public static void main(String[] args) throws ClientException, InterruptedException, IOException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();

        String endpoints = "localhost:8081";
        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
            .setEndpoints(endpoints)
            .build();
        String topic = "example-lite-topic";
        String consumerGroup = "my-lite-push-consumer_example-lite-topic";
        // In most case, you don't need to create too many consumers, singleton pattern is recommended.
        LitePushConsumer litePushConsumer = provider.newLitePushConsumerBuilder()
            .setClientConfiguration(clientConfiguration)
            // Set the consumer group name.
            .setConsumerGroup(consumerGroup)
            // Bind to the parent topic
            .bindTopic(topic)
            .setMessageListener(messageView -> {
                // Handle the received message and return consume result.
                log.info("Consume message={}", messageView);
                return ConsumeResult.SUCCESS;
            })
            .build();

        try {
            /*
            The subscribeLite() method initiates network requests and performs quota verification, so it may fail.
            It's important to check the result of this call to ensure that the subscription was successfully added.
            Possible failure scenarios include:
            1. Network request errors, which can be retried.
            2. Quota verification failures, indicated by LiteSubscriptionQuotaExceededException. In this case,
               evaluate whether the quota is insufficient and promptly unsubscribe from unused subscriptions
               using unsubscribeLite() to free up resources.
            */
            litePushConsumer.subscribeLite("lite-topic-1");
            litePushConsumer.subscribeLite("lite-topic-2");
            litePushConsumer.subscribeLite("lite-topic-3");
        } catch (LiteSubscriptionQuotaExceededException e) {
            // 1. Evaluate and increase the lite topic resource limit.
            // 2. Unsubscribe unused lite topics in time
            // litePushConsumer.unsubscribeLite("lite-topic-3");
            log.error("Lite subscription quota exceeded", e);
        } catch (Exception e) {
            // should retry later
            log.error("Failed to subscribe lite topic", e);
        }

        // Block the main thread, no need for production environment.
        Thread.sleep(Long.MAX_VALUE);
        // Close the push consumer when you don't need it anymore.
        // You could close it manually or add this into the JVM shutdown hook.
        litePushConsumer.close();
    }
}
