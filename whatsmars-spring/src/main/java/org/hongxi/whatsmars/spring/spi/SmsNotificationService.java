package org.hongxi.whatsmars.spring.spi;

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
