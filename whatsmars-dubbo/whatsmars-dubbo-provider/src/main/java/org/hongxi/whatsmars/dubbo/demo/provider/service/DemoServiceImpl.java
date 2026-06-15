package org.hongxi.whatsmars.dubbo.demo.provider.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;

import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.hongxi.whatsmars.dubbo.demo.api.vo.User;

/**
 * Created by shenhongxi on 2017/12/4.
 */
@Slf4j
@DubboService
public class DemoServiceImpl implements DemoService {

    @Override
    public String sayHello(String name) {
        log.info("Hello {}, request from consumer: {}",
                name, RpcContext.getServiceContext().getRemoteAddress());
        return "Hello, " + name;
    }

    @Override
    public String helloContext(String name) {
        log.info("Hello {}, request from consumer: {}, lang: {}",
                name, RpcContext.getServiceContext().getRemoteAddress(),
                RpcContext.getServerAttachment().getAttachment("lang"));
        RpcContext.getServerContext().setAttachment("mode", "Qwen3.7 Max");
        return "Hello, " + name;
    }

    /**
     * 测试是否支持 Java 17 的 record
     */
    @Override
    public User echo(User user) {
        return user;
    }
}