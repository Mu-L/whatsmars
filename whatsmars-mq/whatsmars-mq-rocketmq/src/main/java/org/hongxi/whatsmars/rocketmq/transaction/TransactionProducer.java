package org.hongxi.whatsmars.rocketmq.transaction;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TransactionProducer {
    public static void main(String[] args) throws Exception {
        ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("transaction-check-thread");
                return thread;
            }
        });

        TransactionMQProducer producer = new TransactionMQProducer("transaction_message_producer_group");
        producer.setExecutorService(executorService);
        producer.setTransactionListener(new TransactionListenerImpl());
        producer.start();

        for (int i = 0; i < 10; i++) {
            Message message = new Message("TestTopic4", ("I'm trans message " + i).getBytes(StandardCharsets.UTF_8));
            // 第二个参数可传与本地事务有关的数据，如orderId
            SendResult sendResult = producer.sendMessageInTransaction(message, "3519411001923440");
            System.out.println(sendResult);
        }

        for (int i = 0; i < 100000; i++) {
            Thread.sleep(1000);
        }
        producer.shutdown();
    }
}