package org.hongxi.whatsmars.dubbo.demo.consumer.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.hongxi.whatsmars.dubbo.demo.api.AsyncDemoService;
import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.hongxi.whatsmars.dubbo.demo.api.vo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ConsumerController {

    private static final Logger log = LoggerFactory.getLogger(ConsumerController.class);

    @DubboReference
    private DemoService demoService;

    @DubboReference(async = true)
    private AsyncDemoService asyncDemoService;

    @Autowired
    @Qualifier("commonExecutor")
    private ThreadPoolTaskExecutor executor;

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
    public String helloAsync(String name) {
        log.info("Async calling dubbo provider, {}", name);
        CompletableFuture<String> future = demoService.sayHelloAsync(name);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试 Provider 端限流
     * @param name
     * @return
     */
    @GetMapping("/hello/loop")
    public String helloLoop(String name) {
        log.info("Start to call remote.");
        for (int i = 0; i < 20; i++) {
            try {
                String result = demoService.sayHello(name);
                log.info("Call Count:{} Dubbo Remote Return ======> {}", i, result);
            } catch (RuntimeException ex) {
                if (ex.getMessage().contains("SentinelBlockException: FlowException")) {
                    log.info("Call Count:{} Blocked", i);
                } else {
                    log.error("Call Count:{} Request Failed.", i, ex);
                }
            }
        }
        return "Finished";
    }

    /**
     * 测试 Consumer 端限流、降级
     * @param name
     * @return
     */
    @GetMapping("/hello/concurrent")
    public String helloConcurrent(String name) {
        log.info("Start to call remote.");
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    String result = demoService.slowHello(name);
                    log.info("Call Dubbo Remote Return ======> {}", result);
                } catch (RuntimeException e) {
                    if (e.getMessage().contains("SentinelBlockException: FlowException")) {
                        log.info("Call Blocked (Flow)");
                    } else if (e.getMessage().contains("SentinelBlockException: DegradeException")) {
                        log.info("Call Blocked (Degrade)");
                    } else {
                        log.error("Call Request Failed.", e);
                    }
                }
            });
        }
        return "Started";
    }

    @GetMapping("/echo")
    public User echo() {
        return demoService.echo(new User("lily", 20));
    }

    @GetMapping("/async/hello")
    public String asyncHello(String name) {
        asyncDemoService.sayHello(name);
        CompletableFuture<String> f = RpcContext.getServerContext().getCompletableFuture();
        whenComplete(f);

        CompletableFuture<String> f2 = RpcContext.getServerContext()
                .asyncCall(() -> asyncDemoService.sayHello(name));
        whenComplete(f2);

        CompletableFuture<String> f3 = asyncDemoService.sayHelloAsync(name);
        whenComplete(f3);

        return "OK";
    }

    private void whenComplete(CompletableFuture<?> future) {
        future.whenComplete((v, t) -> {
            if (t != null) {
                log.error("Error", t);
            } else {
                log.info("Result: {}", v);
            }
        });
    }
}
