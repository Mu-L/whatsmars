package org.hongxi.whatsmars.dubbo.idl.demo.provider.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.hongxi.whatsmars.dubbo.idl.greeter.DubboGreeterTriple;
import org.hongxi.whatsmars.dubbo.idl.greeter.GreeterReply;
import org.hongxi.whatsmars.dubbo.idl.greeter.GreeterRequest;

@Slf4j
@DubboService
public class GreeterImpl extends DubboGreeterTriple.GreeterImplBase {
    @Override
    public GreeterReply greet(GreeterRequest request) {
        log.info("Hello {}, request from consumer: {}",
                request.getName(), RpcContext.getServiceContext().getRemoteAddress());
        return GreeterReply.newBuilder().setMessage("Hello " + request.getName()).build();
    }
}
