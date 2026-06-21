package org.hongxi.whatsmars.rocketmq.boot.producer;

import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RocketMQTransactionListener
public class MyTransactionListener implements RocketMQLocalTransactionListener {

    private static final Logger log = LoggerFactory.getLogger(MyTransactionListener.class);

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        String orderId = (String) arg;
        try {
            // 执行本地业务（如更新订单状态）
            log.info("execute local transaction");
            boolean success = updateOrderStatus(orderId, "PAID");
            // 成功则提交消息，消费者可见
            if (success) {
                return RocketMQLocalTransactionState.COMMIT;
            }
        } catch (Exception e) {
            log.error("local transaction execute error", e);
            // 失败则回滚消息
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        return RocketMQLocalTransactionState.UNKNOWN;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        log.info("Received transactional message check, message={}", msg);
        String orderId = msg.getHeaders().get("orderId", String.class);
        // 查询数据库确认订单最终状态
        String status = queryOrderStatus(orderId);
        if ("PAID".equals(status)) {
            log.info("transactional message check success, orderId={}", orderId);
            return RocketMQLocalTransactionState.COMMIT;
        } else if ("FAILED".equals(status)) {
            log.info("rollback transaction");
            return RocketMQLocalTransactionState.ROLLBACK;
        } else {
            // 状态仍不明确，继续等待下次回查
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }

    private boolean updateOrderStatus(String orderId, String status) {
        // 业务逻辑：更新订单状态
        return true;
    }

    private String queryOrderStatus(String orderId) {
        // 业务逻辑：查询订单状态
        return "PAID";
    }
}