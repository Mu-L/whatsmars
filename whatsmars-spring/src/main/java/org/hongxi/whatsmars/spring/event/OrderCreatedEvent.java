package org.hongxi.whatsmars.spring.event;

/**
 * Custom application event for order creation.
 *
 * <p>Since Spring 4.2, events do not need to extend {@link org.springframework.context.ApplicationEvent}.
 * Any plain object can be published as an event. However, extending ApplicationEvent
 * still provides useful context like timestamp and source.
 */
public class OrderCreatedEvent {

    private final String orderId;
    private final String productName;
    private final int quantity;

    public OrderCreatedEvent(String orderId, String productName, int quantity) {
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return "OrderCreatedEvent{orderId='" + orderId + "', product='" + productName + "', quantity=" + quantity + "}";
    }
}
