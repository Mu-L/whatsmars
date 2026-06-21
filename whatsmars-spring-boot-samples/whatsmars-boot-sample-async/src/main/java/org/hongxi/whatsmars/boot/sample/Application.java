package org.hongxi.whatsmars.boot.sample;

import org.hongxi.whatsmars.boot.sample.async.MessageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2020/8/16.
 */
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext =
                SpringApplication.run(Application.class, args);
        MessageService messageService = applicationContext.getBean(MessageService.class);
        CompletableFuture<String> future = messageService.hello("world");
        messageService.send("world");
        try {
            log.info(future.get());
        } catch (InterruptedException | ExecutionException e) {
            log.error("future get error", e);
        }
        messageService.send();
        log.info("......end");
    }
}
