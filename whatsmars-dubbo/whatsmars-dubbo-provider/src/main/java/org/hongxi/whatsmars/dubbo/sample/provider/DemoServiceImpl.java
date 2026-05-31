package org.hongxi.whatsmars.dubbo.sample.provider;

import org.apache.dubbo.rpc.RpcContext;

import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.hongxi.whatsmars.dubbo.demo.api.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoServiceImpl implements DemoService {
    private static final Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Override
    public String sayHello(String name) {
        logger.info("Hello {}, request from consumer: {}",
                name, RpcContext.getServiceContext().getRemoteAddress());
        return "Hello " + name + ", response from provider: "
                + RpcContext.getServiceContext().getLocalAddress();
    }

    @Override
    public User echo(User user) {
        return user;
    }
}