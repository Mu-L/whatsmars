package org.hongxi.whatsmars.dubbo.demo.provider.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;

import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.hongxi.whatsmars.dubbo.demo.api.vo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2017/12/4.
 */
@DubboService
public class DemoServiceImpl implements DemoService {

    private static final Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Override
    public String sayHello(String name) {
        logger.info("Hello " + name + ", request from consumer: "
                + RpcContext.getContext().getRemoteAddress());
        return "Hello " + name;
    }

    @Override
    public User echo(User user) {
        return user;
    }
}