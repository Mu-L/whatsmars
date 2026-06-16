package org.hongxi.whatsmars.dubbo.demo.provider.service;

import org.apache.commons.io.ThreadUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.hongxi.whatsmars.dubbo.demo.api.AsyncDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.*;

@DubboService
public class AsyncDemoServiceImpl implements AsyncDemoService {

    @Autowired
    @Qualifier("commonExecutor")
    private ThreadPoolTaskExecutor executor;

    @Override
    public String sayHello(String name) {
        try {
            long time = ThreadLocalRandom.current().nextLong(1000);
            ThreadUtils.sleep(Duration.ofMillis(time));
            return "AsyncDemoService#sayHello, param: " + name + ", sleep " + time;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public CompletableFuture<String> sayHelloAsync(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                long time = ThreadLocalRandom.current().nextLong(1000);
                ThreadUtils.sleep(Duration.ofMillis(time));
                return "AsyncDemoService#sayHelloAsync, param: " + name + ", sleep " + time;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }, executor);
    }
}
