package org.hongxi.whatsmars.dubbo.demo.api;

import org.apache.dubbo.common.stream.StreamObserver;

public interface StreamingDemoService {

    /**
     * 双向流
     * @param response
     * @return
     */
    StreamObserver<String> sayHelloStream(StreamObserver<String> response);

    /**
     * 服务端流
     * @param request
     * @param response
     */
    void sayHelloServerStream(String request, StreamObserver<String> response);
}
