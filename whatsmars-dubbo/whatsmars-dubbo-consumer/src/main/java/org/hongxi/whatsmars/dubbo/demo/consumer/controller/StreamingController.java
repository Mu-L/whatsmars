package org.hongxi.whatsmars.dubbo.demo.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.hongxi.whatsmars.dubbo.demo.api.StreamingDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class StreamingController {

    @DubboReference
    private StreamingDemoService streamingDemoService;

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
