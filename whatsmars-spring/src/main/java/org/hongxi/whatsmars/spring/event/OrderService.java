package org.hongxi.whatsmars.spring.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service that publishes {@link OrderCreatedEvent} when an order is placed.
 *
 * <p>Spring's {@link ApplicationEventPublisher} is the recommended way to publish events.
 * It is automatically injected by the container. Events are dispatched synchronously by default,
 * meaning the publisher blocks until all listeners have processed the event.
 */
@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public String createOrder(String productName, int quantity) {
        String orderId = UUID.randomUUID().toString().substring(0, 8);
        logger.info("Order created: id={}, product={}, quantity={}", orderId, productName, quantity);

        // Publish event — all matching @EventListener methods will be invoked synchronously
        eventPublisher.publishEvent(new OrderCreatedEvent(orderId, productName, quantity));

        logger.info("Order processing complete: id={}", orderId);
        return orderId;
    }
}
