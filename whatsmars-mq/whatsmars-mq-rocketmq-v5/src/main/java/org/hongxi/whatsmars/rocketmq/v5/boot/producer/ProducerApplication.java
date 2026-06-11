package org.hongxi.whatsmars.rocketmq.v5.boot.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.apis.producer.Transaction;
import org.apache.rocketmq.client.apis.producer.TransactionResolution;
import org.apache.rocketmq.client.common.Pair;
import org.apache.rocketmq.client.core.RocketMQClientTemplate;
import org.apache.rocketmq.client.core.RocketMQTransactionChecker;
import org.hongxi.whatsmars.rocketmq.v5.boot.OrderPaidEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@SpringBootApplication
public class ProducerApplication implements CommandLineRunner {

    private static final String NORMAL_TOPIC = "demo-normal-topic";
    private static final String FIFO_TOPIC = "demo-fifo-topic";
    private static final String DELAY_TOPIC = "demo-delay-topic";
    private static final String TRANS_TOPIC = "demo-trans-topic";

    @Autowired
    private RocketMQClientTemplate rocketMQClientTemplate;

    public static void main(String[] args){
        SpringApplication.run(ProducerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        sendNormalMessage();
        sendFifoMessage();
        sendDelayMessage();
        asyncSendMessage();
        sendTransMessage();
        log.info("send finished!");
    }

    private void sendNormalMessage() {
        rocketMQClientTemplate.syncSendNormalMessage(NORMAL_TOPIC, "I'm normal message");
        rocketMQClientTemplate.syncSendNormalMessage(NORMAL_TOPIC, "I'm normal bytes message".getBytes(StandardCharsets.UTF_8));
        rocketMQClientTemplate.syncSendNormalMessage(NORMAL_TOPIC, MessageBuilder.withPayload("I'm normal spring message").build());
        rocketMQClientTemplate.syncSendNormalMessage(NORMAL_TOPIC, new OrderPaidEvent("T_001", new BigDecimal("188.00")));
    }

    private void sendFifoMessage() {
        rocketMQClientTemplate.syncSendFifoMessage(FIFO_TOPIC, "I'm fifo message", "group1");
        rocketMQClientTemplate.syncSendFifoMessage(FIFO_TOPIC, "I'm fifo bytes message".getBytes(StandardCharsets.UTF_8), "group1");
        rocketMQClientTemplate.syncSendFifoMessage(FIFO_TOPIC, MessageBuilder.withPayload("I'm fifo spring message").build(), "group1");
        rocketMQClientTemplate.syncSendFifoMessage(FIFO_TOPIC, new OrderPaidEvent("T_002", new BigDecimal("288.00")), "group1");
    }

    private void sendDelayMessage() {
        rocketMQClientTemplate.syncSendDelayMessage(DELAY_TOPIC, "I'm delay message", Duration.ofSeconds(5));
        rocketMQClientTemplate.syncSendDelayMessage(DELAY_TOPIC, "I'm delay bytes message".getBytes(StandardCharsets.UTF_8), Duration.ofSeconds(5));
        rocketMQClientTemplate.syncSendDelayMessage(DELAY_TOPIC, MessageBuilder.withPayload("I'm delay spring message").build(), Duration.ofSeconds(5));
        rocketMQClientTemplate.syncSendDelayMessage(DELAY_TOPIC, new OrderPaidEvent("T_003", new BigDecimal("388.00")), Duration.ofSeconds(5));
    }

    private void asyncSendMessage() {
        CompletableFuture<SendReceipt> future0 = new CompletableFuture<>();
        CompletableFuture<SendReceipt> future1 = new CompletableFuture<>();
        CompletableFuture<SendReceipt> future2 = new CompletableFuture<>();
        ExecutorService callbackExecutor = Executors.newCachedThreadPool();

        whenComplete(future0, callbackExecutor);
        whenComplete(future1, callbackExecutor);
        whenComplete(future2, callbackExecutor);

        rocketMQClientTemplate.asyncSendNormalMessage(NORMAL_TOPIC, "I'm normal message", future0);
        rocketMQClientTemplate.asyncSendFifoMessage(FIFO_TOPIC, "I'm fifo message", "group1", future1);
        rocketMQClientTemplate.asyncSendDelayMessage(DELAY_TOPIC, "I'm delay message", Duration.ofSeconds(5), future2);
    }

    private void whenComplete(CompletableFuture<SendReceipt> future, ExecutorService callbackExecutor) {
        future.whenCompleteAsync((sendReceipt, throwable) -> {
            if (throwable != null) {
                log.error("Failed to send message", throwable);
                return;
            }
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        }, callbackExecutor);
    }

    private void sendTransMessage() throws ClientException {
        String orderId = "3519411001923440";
        Message<OrderPaidEvent> message = MessageBuilder.withPayload(new OrderPaidEvent("T_004", new BigDecimal("488.00")))
                .setHeader("orderId", orderId).build();
        Pair<SendReceipt, Transaction> pair = rocketMQClientTemplate.sendTransactionMessage(TRANS_TOPIC, message);
        SendReceipt sendReceipt = pair.getSendReceipt();
        log.info("transactionSend to topic {} sendReceipt={}", TRANS_TOPIC, sendReceipt);
        Transaction transaction = pair.getTransaction();
        // executed local transaction
        if (doLocalTransaction(orderId)) {
            transaction.commit();
        } else {
            transaction.rollback();
        }
    }

    private boolean doLocalTransaction(String orderId) {
        log.info("execute local transaction");
        return orderId != null;
    }

    /**
     * 该注解的属性 rocketMQTemplateBeanName 指定了使用的 RocketMQClientTemplate 实例
     * 这说明了在5.0中，事务检查器绑定了 Producer 实例，而 Broker 拥有 Producer 信息
     * 因此 Broker 知道该回调哪个事务检查器。
     * 在4.0中，事务检查器是通过 producer group 来关联 Producer 的。
     *
     * 理论上，一个 RocketMQClientTemplate 可以用于多个事务 Topic
     * 但那样需要在同一个事务检查器里针各 Topic 写各自的 check 逻辑
     */
    @RocketMQTransactionListener
    static class TransactionListenerImpl implements RocketMQTransactionChecker {
        @Override
        public TransactionResolution check(MessageView messageView) {
            log.info("Received transactional message check, message={}", messageView);
            if (Objects.nonNull(messageView.getProperties().get("orderId"))) {
                log.info("transactional message check success, messageId={}", messageView.getMessageId());
                return TransactionResolution.COMMIT;
            }
            log.info("rollback transaction");
            return TransactionResolution.ROLLBACK;
        }
    }
}
