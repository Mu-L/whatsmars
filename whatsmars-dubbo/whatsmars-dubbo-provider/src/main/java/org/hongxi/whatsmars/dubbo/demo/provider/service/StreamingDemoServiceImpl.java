package org.hongxi.whatsmars.dubbo.demo.provider.service;

import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;
import org.hongxi.whatsmars.dubbo.demo.api.StreamingDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DubboService
public class StreamingDemoServiceImpl implements StreamingDemoService {

    private static final Logger log = LoggerFactory.getLogger(StreamingDemoServiceImpl.class);
    @Override
    public StreamObserver<String> sayHelloStream(StreamObserver<String> response) {
        return new StreamObserver<>() {
            @Override
            public void onNext(String data) {
                log.info("data: {}", data);
                response.onNext(data);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("onError", throwable);
            }

            @Override
            public void onCompleted() {
                log.info("onCompleted");
                response.onCompleted();
            }
        };
    }

    @Override
    public void sayHelloServerStream(String request, StreamObserver<String> response) {
        log.info("received request: {}", request);
        for (int i = 0; i < 10; i++) {
            response.onNext(request + "-" + i);
        }
        response.onCompleted();
    }
}
