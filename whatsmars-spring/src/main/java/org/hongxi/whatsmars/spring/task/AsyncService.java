package org.hongxi.whatsmars.spring.task;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Demonstrates {@code @Async} for asynchronous method execution.
 *
 * <p>Requires {@code @EnableAsync} on a configuration class.</p>
 * <ul>
 *   <li>{@code void} methods run asynchronously (fire-and-forget)</li>
 *   <li>Methods returning {@link Future}/{@link CompletableFuture} allow
 *       the caller to retrieve the result later</li>
 * </ul>
 */
@Service
public class AsyncService {

    @Async
    public void doAsyncWork(String taskName) {
        System.out.printf("[@Async] '%s' running on thread: %s%n",
                taskName, Thread.currentThread().getName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.printf("[@Async] '%s' completed%n", taskName);
    }

    @Async
    public CompletableFuture<String> computeAsync(int input) {
        System.out.printf("[@Async] computeAsync(%d) on thread: %s%n",
                input, Thread.currentThread().getName());
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture("result=" + (input * 2));
    }
}
