package org.hongxi.whatsmars.dubbo.idl.demo.consumer.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.hongxi.whatsmars.dubbo.idl.unary.Greeter;
import org.hongxi.whatsmars.dubbo.idl.unary.GreeterRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class ConsumerController {

    private static final Logger log = LoggerFactory.getLogger(ConsumerController.class);

    @DubboReference
    private Greeter greeter;

    @GetMapping("/hello")
    public String hello(String name) {
        log.info("Calling dubbo provider, {}", name);
        GreeterRequest request = GreeterRequest.newBuilder().setName(name).build();
        return greeter.greet(request).getMessage();
    }
}
