package org.hongxi.whatsmars.rocketmq.boot;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderPaidEvent(String orderId, BigDecimal paidMoney) implements Serializable {
}