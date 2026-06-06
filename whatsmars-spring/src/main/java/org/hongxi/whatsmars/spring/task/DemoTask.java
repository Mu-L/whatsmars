package org.hongxi.whatsmars.spring.task;

/**
 * Simple {@link Runnable} task used by the context demo
 * to demonstrate async execution via {@code ThreadPoolTaskExecutor}.
 */
public class DemoTask implements Runnable {
    @Override
    public void run() {
        System.out.println("demo task...");
    }
}
