package org.hongxi.whatsmars.dubbo.idl.demo.provider.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.hongxi.whatsmars.dubbo.idl.unary.DubboGreeterTriple;
import org.hongxi.whatsmars.dubbo.idl.unary.GreeterReply;
import org.hongxi.whatsmars.dubbo.idl.unary.GreeterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DubboService
public class GreeterImpl extends DubboGreeterTriple.GreeterImplBase {

    private static final Logger log = LoggerFactory.getLogger(GreeterImpl.class);
    @Override
    public GreeterReply greet(GreeterRequest request) {
        log.info("Hello {}, request from consumer: {}",
                request.getName(), RpcContext.getServiceContext().getRemoteAddress());
        return GreeterReply.newBuilder().setMessage("Hello " + request.getName()).build();
    }
}
