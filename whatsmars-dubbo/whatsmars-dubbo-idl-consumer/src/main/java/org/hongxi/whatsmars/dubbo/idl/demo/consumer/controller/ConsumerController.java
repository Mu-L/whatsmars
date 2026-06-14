package org.hongxi.whatsmars.dubbo.idl.demo.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.hongxi.whatsmars.dubbo.idl.unary.Greeter;
import org.hongxi.whatsmars.dubbo.idl.unary.GreeterRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ConsumerController {

    @DubboReference
    private Greeter greeter;

    @GetMapping("/hello")
    public String hello(String name) {
        log.info("Calling dubbo provider, {}", name);
        GreeterRequest request = GreeterRequest.newBuilder().setName(name).build();
        return greeter.greet(request).getMessage();
    }
}
