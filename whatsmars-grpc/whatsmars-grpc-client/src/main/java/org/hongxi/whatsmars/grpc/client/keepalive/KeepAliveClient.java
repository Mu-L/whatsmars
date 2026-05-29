package org.hongxi.whatsmars.grpc.client.keepalive;

import io.grpc.Channel;
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
 * A simple client that requests a greeting from the {KeepAliveServer.
 */
public class KeepAliveClient {
    private static final Logger logger = LoggerFactory.getLogger(KeepAliveClient.class);

    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public KeepAliveClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    /**
     * Say hello to server.
     */
    public void greet(String name) {
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
     * Greet server.
     */
    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";

        // Create a channel with the following keep alive configurations (demo only, you should set
        // more appropriate values based on your environment):
        // keepAliveTime: Send pings every 10 seconds if there is no activity. Set to an appropriate
        // value in reality, e.g. (5, TimeUnit.MINUTES).
        // keepAliveTimeout: Wait 1 second for ping ack before considering the connection dead. Set to a
        // larger value in reality, e.g. (10, TimeUnit.SECONDS). You should only set such a small value,
        // e.g. (1, TimeUnit.SECONDS) in certain low latency environments.
        // keepAliveWithoutCalls: Send pings even without active streams. Normally disable it.
        // Use JAVA_OPTS=-Djava.util.logging.config.file=logging.properties to see the keep alive ping
        // frames.
        // More details see: https://github.com/grpc/proposal/blob/master/A8-client-side-keepalive.md
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .keepAliveTime(10, TimeUnit.SECONDS) // Change to a larger value, e.g. 5min.
                .keepAliveTimeout(1, TimeUnit.SECONDS) // Change to a larger value, e.g. 10s.
                .keepAliveWithoutCalls(true)// You should normally avoid enabling this.
                .build();

        try {
            KeepAliveClient client = new KeepAliveClient(channel);
            client.greet("Keep-alive Demo");
            Thread.sleep(30000);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}