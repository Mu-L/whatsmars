package org.hongxi.whatsmars.spring.spi;

/**
 * SMS implementation of {@link NotificationService}.
 * Registered via {@code META-INF/spring.factories}.
 */
public class SmsNotificationService implements NotificationService {

    @Override
    public String name() {
        return "SMS";
    }

    @Override
    public void send(String message) {
        System.out.println("  [SMS] sending: " + message);
    }
}
