package org.hongxi.whatsmars.dubbo.demo.consumer;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.hongxi.whatsmars.dubbo.demo.api.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> {
            logger.info("result: {}", demoService.sayHello("world"));

            CompletableFuture<String> future = demoService.sayHelloAsync("world");
            try {
                logger.info("async call returned: {}", future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            logger.info("echo: {}", demoService.echo(new User("lily", 20)));
        };
    }
}
