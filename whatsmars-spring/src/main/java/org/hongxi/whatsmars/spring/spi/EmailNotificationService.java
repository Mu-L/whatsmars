package org.hongxi.whatsmars.spring.spi;

/**
 * Email implementation of {@link NotificationService}.
 * Registered via {@code META-INF/spring.factories}.
 */
public class EmailNotificationService implements NotificationService {

    @Override
    public String name() {
        return "Email";
    }

    @Override
    public void send(String message) {
        System.out.println("  [Email] sending: " + message);
    }
}
