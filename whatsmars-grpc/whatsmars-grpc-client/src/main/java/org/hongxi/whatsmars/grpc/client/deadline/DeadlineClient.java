package org.hongxi.whatsmars.grpc.client.deadline;

import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A simple client that requests a greeting from the HelloWorldServer.
 *
 * <p>This is based off the client in the helloworld example with some deadline logic added.
 */
public class DeadlineClient {
    private static final Logger logger = LoggerFactory.getLogger(DeadlineClient.class);

    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public DeadlineClient(Channel channel) {
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    /**
     * Say hello to server.
     */
    public Status greet(String name, long timeoutMillis) {
        logger.info("Will try to greet {} ...", name);
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = blockingStub.withDeadlineAfter(timeoutMillis, TimeUnit.MILLISECONDS)
                    .sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return e.getStatus();
        }
        logger.info("Greeting: {}", response.getMessage());
        return Status.OK;
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */
    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");

        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        try {
            DeadlineClient client = new DeadlineClient(channel);

            // The server takes 500ms to process the call, so setting a deadline further in the future we
            // should get a successful response.
            logger.info("Calling server with a generous deadline, expected to work");
            client.greet("deadline client", 1000);

            // A smaller deadline will result in us getting a DEADLINE_EXCEEDED error.
            logger.info("Calling server with an unrealistic (300ms) deadline, expecting a DEADLINE_EXCEEDED");
            client.greet("deadline client", 300);

            // Including the "propagate" magic string in the request will cause the server to call itself
            // to simulate a situation where a server needs to call another server to satisfy the original
            // request. This will double the time it takes to respond to the client request, but with
            // an increased deadline we should get a successful response.
            logger.info("Calling server with propagation and a generous deadline, expected to work");
            client.greet("deadline client [propagate]", 2000);

            // With this propagated call we reduce the deadline making it impossible for the both the
            // first server call and the propagated one to succeed. You should see the call fail with
            // DEADLINE_EXCEEDED, and you should also see DEADLINE_EXCEEDED in the server output as it
            // runs out of time waiting for the propagated call to finish.
            logger.info("Calling server with propagation and a generous deadline, expecting a DEADLINE_EXCEEDED");
            client.greet("deadline client [propagate]", 1000);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}