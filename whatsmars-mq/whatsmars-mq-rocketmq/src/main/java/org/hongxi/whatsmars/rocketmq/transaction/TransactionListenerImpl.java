package org.hongxi.whatsmars.rocketmq.transaction;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionListenerImpl implements TransactionListener {
    private final AtomicInteger transactionIndex = new AtomicInteger(0);

    // key: 事务id  value: 本地事务执行结果
    private final ConcurrentMap<String, Integer> localTrans = new ConcurrentHashMap<>();

    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        int value = transactionIndex.getAndIncrement();
        int status = value % 3;
        localTrans.put(msg.getTransactionId(), status);
        // return UNKNOW is just for test
        return LocalTransactionState.UNKNOW;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        Integer status = localTrans.get(msg.getTransactionId());
        if (status != null) {
            return switch (status) {
                case 0 -> LocalTransactionState.UNKNOW;
                case 2 -> LocalTransactionState.ROLLBACK_MESSAGE;
                default -> LocalTransactionState.COMMIT_MESSAGE;
            };
        }
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }
}