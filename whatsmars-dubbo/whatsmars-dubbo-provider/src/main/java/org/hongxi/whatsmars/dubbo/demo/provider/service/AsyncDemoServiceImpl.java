package org.hongxi.whatsmars.dubbo.demo.provider.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.hongxi.whatsmars.dubbo.demo.api.AsyncDemoService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@DubboService
public class AsyncDemoServiceImpl implements AsyncDemoService {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public String sayHello(String name) {
        try {
            long time = ThreadLocalRandom.current().nextLong(1000);
            Thread.sleep(time);
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
                Thread.sleep(time);
                return "AsyncDemoService#sayHelloAsync, param: " + name + ", sleep " + time;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }, executorService);
    }
}
