package org.hongxi.whatsmars.dubbo.demo.provider.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.remoting.http12.HttpMethods;
import org.apache.dubbo.remoting.http12.rest.Mapping;
import org.apache.dubbo.remoting.http12.rest.Param;
import org.apache.dubbo.remoting.http12.rest.ParamType;
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

    /**
     * http://localhost:50051/org.hongxi.whatsmars.dubbo.demo.api.DemoService/hello/lily
     */
    @Mapping(path = "/hello/{name}", method = HttpMethods.GET)
    @Override
    public String sayHello(@Param(value = "name", type = ParamType.PathVariable) String name) {
        logger.info("Hello {}, request from consumer: {}",
                name, RpcContext.getServiceContext().getRemoteAddress());
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