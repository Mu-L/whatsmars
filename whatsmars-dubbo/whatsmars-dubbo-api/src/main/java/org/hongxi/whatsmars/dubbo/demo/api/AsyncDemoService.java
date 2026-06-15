package org.hongxi.whatsmars.dubbo.demo.api;

import java.util.concurrent.CompletableFuture;

public interface AsyncDemoService {

    String sayHello(String name);

    CompletableFuture<String> sayHelloAsync(String name);
}
