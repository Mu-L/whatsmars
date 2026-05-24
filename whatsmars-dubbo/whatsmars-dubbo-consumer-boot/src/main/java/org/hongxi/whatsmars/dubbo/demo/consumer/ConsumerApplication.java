package org.hongxi.whatsmars.dubbo.demo.consumer;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Created by javahongxi on 2017/12/4.
 */
@SpringBootApplication
@EnableDubbo
public class ConsumerApplication {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerApplication.class);

    @DubboReference
    private DemoService demoService;

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(ConsumerApplication.class, args);
        ConsumerApplication application = context.getBean(ConsumerApplication.class);
        String result = application.doSayHello("world");
        logger.info("result: {}", result);

        CompletableFuture<String> future = application.doSayHelloAsync("world");
        try {
            logger.info("async call returned: {}", future.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String doSayHello(String name) {
        return demoService.sayHello(name);
    }

    public CompletableFuture<String> doSayHelloAsync(String name) {
        return demoService.sayHelloAsync(name);
    }
}
