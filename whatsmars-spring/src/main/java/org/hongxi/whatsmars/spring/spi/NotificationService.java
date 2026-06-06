package org.hongxi.whatsmars.spring.spi;

/**
 * SPI contract: a notification service that can have multiple implementations.
 *
 * <p>Implementations are discovered via {@code META-INF/spring.factories},
 * similar to how Spring Boot discovers auto-configurations.</p>
 */
public interface NotificationService {

    /**
     * @return the name of this notification service
     */
    String name();

    /**
     * Send a notification.
     */
    void send(String message);
}
