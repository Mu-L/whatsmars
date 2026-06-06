package org.hongxi.whatsmars.spring.spi;

import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.List;

/**
 * Demonstrates Spring's SPI mechanism via {@link SpringFactoriesLoader}:
 * <ul>
 *   <li>Defines an interface ({@link NotificationService})</li>
 *   <li>Provides multiple implementations</li>
 *   <li>Registers implementations in {@code META-INF/spring.factories}</li>
 *   <li>Discovers and loads all implementations at runtime</li>
 * </ul>
 *
 * <p>This is the same mechanism Spring Boot uses for auto-configuration discovery.</p>
 */
public class SpiDemo {

    public static void main(String[] args) {
        System.out.println("===== SpringFactoriesLoader SPI Demo =====\n");

        // Discover all implementations of NotificationService
        List<NotificationService> services = SpringFactoriesLoader.loadFactories(
                NotificationService.class, SpiDemo.class.getClassLoader());

        System.out.println("Discovered " + services.size() + " NotificationService implementation(s):\n");

        for (NotificationService service : services) {
            System.out.println("  [" + service.name() + "] " + service.getClass().getSimpleName());
            service.send("Hello from SPI demo!");
        }

        System.out.println("\nDone.");
    }
}
