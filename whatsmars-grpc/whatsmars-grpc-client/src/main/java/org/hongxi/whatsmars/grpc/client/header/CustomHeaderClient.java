package org.hongxi.whatsmars.grpc.client.header;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A simple client that like HelloWorldClient.
 * This client can help you create custom headers.
 */
public class CustomHeaderClient {
    private static final Logger logger = LoggerFactory.getLogger(CustomHeaderClient.class);

    private final ManagedChannel originChannel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * A custom client.
     */
    private CustomHeaderClient(String host, int port) {
        originChannel = Grpc
                .newChannelBuilderForAddress(host, port, InsecureChannelCredentials.create())
                .build();
        ClientInterceptor interceptor = new HeaderClientInterceptor();
        Channel channel = ClientInterceptors.intercept(originChannel, interceptor);
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    private void shutdown() throws InterruptedException {
        originChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * A simple client method that like HelloWorldClient.
     */
    private void greet(String name) {
        logger.info("Will try to greet {} ...", name);
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

    /**
     * Main start the client from the command line.
     */
    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        CustomHeaderClient client = new CustomHeaderClient("localhost", 50051);
        try {
            String user = "world";
            // Use the arg as the name to greet if provided
            if (args.length > 0) {
                user = args[0];
            }
            client.greet(user);
        } finally {
            client.shutdown();
        }
    }
}