package org.hongxi.whatsmars.dubbo.idl.demo.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboReference;
import org.hongxi.whatsmars.dubbo.idl.streaming.GreeterRequest;
import org.hongxi.whatsmars.dubbo.idl.streaming.GreeterReply;
import org.hongxi.whatsmars.dubbo.idl.streaming.StreamingGreeter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@Slf4j
@RestController
public class StreamingController {

    @DubboReference
    private StreamingGreeter streamingGreeter;

    @GetMapping("/stream")
    public String biStream(String name) {
        log.info("Calling dubbo provider, {}", name);
        CountDownLatch latch = new CountDownLatch(10);
        StreamObserver<GreeterRequest> requestStreamObserver = streamingGreeter.biStream(new SampleStreamObserver(latch));
        for (int i = 0; i < 10; i++) {
            GreeterRequest request = GreeterRequest.newBuilder().setName(name + "-" + i).build();
            requestStreamObserver.onNext(request);
        }
        requestStreamObserver.onCompleted();
        return "OK";
    }

    @GetMapping("/stream/server")
    public String serverStream(String name) {
        log.info("Calling dubbo provider, {}", name);
        CountDownLatch latch = new CountDownLatch(10);
        GreeterRequest request = GreeterRequest.newBuilder().setName(name).build();
        streamingGreeter.serverStream(request, new SampleStreamObserver(latch));
        return "OK";
    }

    private static class SampleStreamObserver implements StreamObserver<GreeterReply> {

        private final CountDownLatch latch;

        public SampleStreamObserver(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onNext(GreeterReply data) {
            log.info("stream <- reply: {}", data);
            latch.countDown();
        }

        @Override
        public void onError(Throwable throwable) {
            log.error("stream onError", throwable);
        }

        @Override
        public void onCompleted() {
            log.info("stream completed");
        }
    }
}
