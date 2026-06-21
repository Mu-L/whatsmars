package org.hongxi.whatsmars.dubbo.idl.demo.provider.service;

import org.apache.dubbo.common.stream.StreamObserver;
import org.apache.dubbo.config.annotation.DubboService;
import org.hongxi.whatsmars.dubbo.idl.streaming.DubboStreamingGreeterTriple;
import org.hongxi.whatsmars.dubbo.idl.streaming.GreeterReply;
import org.hongxi.whatsmars.dubbo.idl.streaming.GreeterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DubboService
public class StreamingGreeterImpl extends DubboStreamingGreeterTriple.StreamingGreeterImplBase {

    private static final Logger log = LoggerFactory.getLogger(StreamingGreeterImpl.class);

    @Override
    public StreamObserver<GreeterRequest> biStream(StreamObserver<GreeterReply> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(GreeterRequest data) {
                log.info("data: {}", data);
                GreeterReply resp = GreeterReply.newBuilder().setMessage(data.getName()).build();
                responseObserver.onNext(resp);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    @Override
    public void serverStream(GreeterRequest request, StreamObserver<GreeterReply> responseObserver) {
        log.info("received request: {}", request);
        for (int i = 0; i < 10; i++) {
            GreeterReply reply = GreeterReply.newBuilder().setMessage(request.getName() + "-" + i).build();
            responseObserver.onNext(reply);
        }
        responseObserver.onCompleted();
    }
}
