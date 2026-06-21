package org.hongxi.whatsmars.dubbo.demo.provider.service;

import org.apache.commons.io.ThreadUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;

import org.hongxi.whatsmars.dubbo.demo.api.DemoService;
import org.hongxi.whatsmars.dubbo.demo.api.vo.User;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2017/12/4.
 */
@DubboService
public class DemoServiceImpl implements DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoServiceImpl.class);

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

    @Override
    public String slowHello(String name) {
        long time = ThreadLocalRandom.current().nextLong(1000);
        try {
            ThreadUtils.sleep(Duration.ofMillis(time));
            return "Hello, " + name + ", sleep " + time;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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