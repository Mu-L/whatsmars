package org.hongxi.whatsmars.grpc.client.nameresolve;

import io.grpc.*;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class NameResolveClient {
    private static final Logger logger = LoggerFactory.getLogger(NameResolveClient.class);

    public static final String channelTarget = "example:///lb.example.grpc.io";
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    public NameResolveClient(Channel channel) {
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) throws Exception {
        NameResolverRegistry.getDefaultRegistry().register(new ExampleNameResolverProvider());

        logger.info("Use default DNS resolver");
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:50051")
                .usePlaintext()
                .build();
        try {
            NameResolveClient client = new NameResolveClient(channel);
            for (int i = 0; i < 5; i++) {
                client.greet("request" + i);
            }
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }

        logger.info("Change to use example name resolver");
        /*
          Dial to "example:///resolver.example.grpc.io", use {@link ExampleNameResolver} to create connection
          "resolver.example.grpc.io" is converted to {@link java.net.URI.path}
         */
        channel = ManagedChannelBuilder.forTarget(channelTarget)
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .build();
        try {
            NameResolveClient client = new NameResolveClient(channel);
            for (int i = 0; i < 5; i++) {
                client.greet("request" + i);
            }
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
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
        logger.info("Greeting: {}", response.getMessage());
    }
}