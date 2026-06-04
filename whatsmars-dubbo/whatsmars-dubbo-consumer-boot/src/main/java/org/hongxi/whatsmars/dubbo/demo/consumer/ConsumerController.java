package org.hongxi.whatsmars.dubbo.demo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.hongxi.whatsmars.dubbo.demo.api.vo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
public class ConsumerController {

    @DubboReference
    private DemoService demoService;

    @GetMapping("/hello")
    public String hello(String name) {
        log.info("Calling dubbo provider, {}", name);
        return demoService.sayHello(name);
    }

    @GetMapping("/hello/async")
    public String asyncHello(String name) {
        log.info("Async calling dubbo provider, {}", name);
        CompletableFuture<String> future = demoService.sayHelloAsync(name);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/echo")
    public User echo() {
        return demoService.echo(new User("lily", 20));
    }
}
