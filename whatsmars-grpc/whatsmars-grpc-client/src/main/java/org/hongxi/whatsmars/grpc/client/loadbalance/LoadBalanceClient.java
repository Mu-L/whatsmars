package org.hongxi.whatsmars.grpc.client.loadbalance;

import io.grpc.*;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class LoadBalanceClient {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalanceClient.class);

    public static final String exampleScheme = "example";
    public static final String exampleServiceName = "lb.example.grpc.io";

    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    public LoadBalanceClient(Channel channel) {
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getMessage());
    }


    public static void main(String[] args) throws Exception {
        NameResolverRegistry.getDefaultRegistry().register(new ExampleNameResolverProvider());

        String target = String.format("%s:///%s", exampleScheme, exampleServiceName);

        logger.info("Use default first_pick load balance policy");
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        try {
            LoadBalanceClient client = new LoadBalanceClient(channel);
            for (int i = 0; i < 5; i++) {
                client.greet("request" + i);
            }
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }

        logger.info("Change to round_robin policy");
        channel = ManagedChannelBuilder.forTarget(target)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();
        try {
            LoadBalanceClient client = new LoadBalanceClient(channel);
            for (int i = 0; i < 5; i++) {
                client.greet("request" + i);
            }
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}