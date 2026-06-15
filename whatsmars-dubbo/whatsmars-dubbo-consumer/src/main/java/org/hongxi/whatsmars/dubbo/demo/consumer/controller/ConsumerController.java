package org.hongxi.whatsmars.dubbo.demo.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.hongxi.whatsmars.dubbo.demo.api.StreamingDemoService;
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
    @DubboReference
    private StreamingDemoService streamingDemoService;

    @GetMapping("/hello")
    public String hello(String name) {
        log.info("Calling dubbo provider, {}", name);
        return demoService.sayHello(name);
    }

    @GetMapping("/hello/context")
    public String helloContext(String name) {
        log.info("Calling dubbo provider, {}", name);
        RpcContext.getClientAttachment().setAttachment("lang", "zh");
        String result = demoService.helloContext(name);
        log.info("mode: {}", RpcContext.getServerContext().getAttachment("mode"));
        return result;
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

    @GetMapping("/stream")
    public String sayHelloStream(String name) {
        log.info("Calling dubbo provider, {}", name);
        StreamObserver<String> request = streamingDemoService.sayHelloStream(new StreamObserver<>() {
            @Override
            public void onNext(String data) {
                log.info("data: {}", data);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("onError", throwable);
            }

            @Override
            public void onCompleted() {
                log.info("onCompleted");
            }
        });
        for (int i = 0; i < 10; i++) {
            request.onNext(name + "-" + i);
        }
        request.onCompleted();
        return "OK";
    }

    @GetMapping("/stream/server")
    public String sayHelloServerStream(String name) {
        streamingDemoService.sayHelloServerStream(name, new StreamObserver<>() {
            @Override
            public void onNext(String data) {
                log.info("data: {}", data);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("onError", throwable);
            }

            @Override
            public void onCompleted() {
                log.info("onCompleted");
            }
        });
        return "OK";
    }
}
