package org.hongxi.whatsmars.spring.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates {@code @Scheduled} for periodic task execution.
 *
 * <p>Requires {@code @EnableScheduling} on a configuration class.</p>
 * <ul>
 *   <li>{@code fixedRate} — interval between the <b>start</b> of each invocation</li>
 *   <li>{@code fixedDelay} — interval between the <b>end</b> of one and the <b>start</b> of the next</li>
 *   <li>{@code initialDelay} — wait time before the first execution</li>
 * </ul>
 */
@Component
public class ScheduledTask {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Scheduled(fixedRate = 1000, initialDelay = 500)
    public void fixedRateTask() {
        int count = counter.incrementAndGet();
        System.out.printf("[@Scheduled fixedRate] #%d at %s on thread: %s%n",
                count, LocalTime.now(), Thread.currentThread().getName());
    }
}
