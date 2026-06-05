package org.hongxi.whatsmars.spring.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listens for {@link OrderCreatedEvent} using the modern {@code @EventListener} annotation.
 *
 * <p>{@code @EventListener} was introduced in Spring 4.2 as a replacement for the older
 * {@link org.springframework.context.ApplicationListener} interface. Key advantages:
 * <ul>
 *   <li>No need to implement an interface</li>
 *   <li>Supports SpEL conditions via {@code @EventListener(condition = "...")}</li>
 *   <li>Can listen to plain objects (not just ApplicationEvent subclasses)</li>
 *   <li>Multiple listeners can be ordered using {@code @Order}</li>
 * </ul>
 */
@Component
public class OrderEventListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    /**
     * Inventory listener — runs first (higher priority via @Order).
     * Simulates reserving stock for the order.
     */
    @EventListener
    @Order(1)
    public void handleInventory(OrderCreatedEvent event) {
        logger.info("[Inventory] Reserving {} x '{}' for order {}",
                event.getQuantity(), event.getProductName(), event.getOrderId());
    }

    /**
     * Notification listener — runs second.
     * Simulates sending an order confirmation.
     */
    @EventListener
    @Order(2)
    public void handleNotification(OrderCreatedEvent event) {
        logger.info("[Notification] Order {} confirmed: {} x '{}'",
                event.getOrderId(), event.getQuantity(), event.getProductName());
    }

    /**
     * Analytics listener with SpEL condition — only fires for large orders (quantity >= 10).
     * Demonstrates conditional event handling.
     */
    @EventListener(condition = "#event.quantity >= 10")
    @Order(3)
    public void handleLargeOrderAlert(OrderCreatedEvent event) {
        logger.info("[Analytics] Large order detected! Order {} has quantity={}",
                event.getOrderId(), event.getQuantity());
    }
}
