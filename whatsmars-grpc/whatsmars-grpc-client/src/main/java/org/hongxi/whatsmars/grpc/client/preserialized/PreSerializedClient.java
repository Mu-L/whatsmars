package org.hongxi.whatsmars.grpc.client.preserialized;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import io.grpc.stub.ClientCalls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A client that requests a greeting from a hello-world server, but using a pre-serialized request.
 * This is a performance optimization that can be useful if you read the request from on-disk or a
 * database where it is already serialized, or if you need to send the same complicated message to
 * many servers. The same approach can avoid deserializing responses, to be stored in a database.
 * This adjustment is client-side only; the server is unable to detect the difference, so this
 * client is fully-compatible with the normal HelloWorldServer.
 */
public class PreSerializedClient {
    private static final Logger logger = LoggerFactory.getLogger(PreSerializedClient.class);

    /**
     * Modified sayHello() descriptor with bytes as the request, instead of HelloRequest. By adjusting
     * toBuilder() you can choose which of the request and response are bytes.
     */
    private static final MethodDescriptor<byte[], HelloReply> SAY_HELLO
            = GreeterGrpc.getSayHelloMethod()
            .toBuilder(new ByteArrayMarshaller(), GreeterGrpc.getSayHelloMethod().getResponseMarshaller())
            .build();

    private final Channel channel;

    /**
     * Construct client for accessing hello-world server using the existing channel.
     */
    public PreSerializedClient(Channel channel) {
        this.channel = channel;
    }

    /**
     * Say hello to server.
     */
    public void greet(String name) {
        logger.info("Will try to greet {} ...", name);
        byte[] request = HelloRequest.newBuilder().setName(name).build().toByteArray();
        HelloReply response;
        try {
            // Stubs use ClientCalls to send RPCs. Since the generated stub won't have byte[] in its
            // method signature, this uses ClientCalls directly. It isn't as convenient, but it behaves
            // the same as a normal stub.
            response = ClientCalls.blockingUnaryCall(channel, SAY_HELLO, CallOptions.DEFAULT, request);
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return;
        }
        logger.info("Greeting: {}", response.getMessage());
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */
    public static void main(String[] args) throws Exception {
        String user = "world";
        String target = "localhost:50051";
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [name [target]]");
                System.err.println("");
                System.err.println("  name    The name you wish to be greeted by. Defaults to " + user);
                System.err.println("  target  The server to connect to. Defaults to " + target);
                System.exit(1);
            }
            user = args[0];
        }
        if (args.length > 1) {
            target = args[1];
        }

        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        try {
            PreSerializedClient client = new PreSerializedClient(channel);
            client.greet(user);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}