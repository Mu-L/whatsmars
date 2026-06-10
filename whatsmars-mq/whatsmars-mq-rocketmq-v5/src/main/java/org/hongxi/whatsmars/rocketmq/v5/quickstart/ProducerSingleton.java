package org.hongxi.whatsmars.rocketmq.v5.quickstart;

import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.ProducerBuilder;
import org.apache.rocketmq.client.apis.producer.TransactionChecker;

/**
 * Each client will establish an independent connection to the server node within a process.
 *
 * <p>In most cases, the singleton mode can meet the requirements of higher concurrency.
 * If multiple connections are desired, consider increasing the number of clients appropriately.
 */
public class ProducerSingleton {
    private static volatile Producer PRODUCER;
    private static volatile Producer TRANSACTIONAL_PRODUCER;
    private static final String ENDPOINTS = "localhost:8081";

    private ProducerSingleton() {
    }

    private static Producer buildProducer(TransactionChecker checker, String... topics) throws ClientException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
            .setEndpoints(ENDPOINTS)
            .build();
        final ProducerBuilder builder = provider.newProducerBuilder()
            .setClientConfiguration(clientConfiguration)
            // Set the topic name(s), which is optional but recommended. It makes producer could prefetch
            // the topic route before message publishing.
            // For transaction producers, it is essential to set topics to ensure the reliability of the transaction
            // checker.
            .setTopics(topics);
        if (checker != null) {
            // Set the transaction checker.
            builder.setTransactionChecker(checker);
        }
        return builder.build();
    }

    public static Producer getInstance(String... topics) throws ClientException {
        if (PRODUCER == null) {
            synchronized (ProducerSingleton.class) {
                if (PRODUCER == null) {
                    PRODUCER = buildProducer(null, topics);
                }
            }
        }
        return PRODUCER;
    }

    public static Producer getTransactionalInstance(TransactionChecker checker,
        String... topics) throws ClientException {
        if (TRANSACTIONAL_PRODUCER == null) {
            synchronized (ProducerSingleton.class) {
                if (TRANSACTIONAL_PRODUCER == null) {
                    TRANSACTIONAL_PRODUCER = buildProducer(checker, topics);
                }
            }
        }
        return TRANSACTIONAL_PRODUCER;
    }
}
