package org.hongxi.whatsmars.rocketmq.v5.boot;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderPaidEvent(String orderId, BigDecimal paidMoney) implements Serializable {
}