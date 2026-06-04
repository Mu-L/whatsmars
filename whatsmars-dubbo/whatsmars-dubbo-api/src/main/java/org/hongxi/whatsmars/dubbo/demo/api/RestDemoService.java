package org.hongxi.whatsmars.dubbo.demo.api;

import org.hongxi.whatsmars.dubbo.demo.api.vo.EchoRequest;

public interface RestDemoService {

    String hello(String name);

    int add(int a, int b);

    String echo(EchoRequest request);

    String greet(String name, String lang);
}