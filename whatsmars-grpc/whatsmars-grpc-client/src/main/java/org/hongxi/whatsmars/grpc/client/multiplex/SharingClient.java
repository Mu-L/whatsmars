package org.hongxi.whatsmars.grpc.client.multiplex;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.AbstractFuture;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import org.hongxi.whatsmars.grpc.api.echo.EchoGrpc;
import org.hongxi.whatsmars.grpc.api.echo.EchoRequest;
import org.hongxi.whatsmars.grpc.api.echo.EchoResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;


/**
 * A client that shares a channel across multiple stubs to a single service and across services
 * being provided by one server process.
 */
public class SharingClient {
    private static final Logger logger = LoggerFactory.getLogger(SharingClient.class);

    private final GreeterGrpc.GreeterBlockingStub greeterStub1;
    private final GreeterGrpc.GreeterBlockingStub greeterStub2;
    private final EchoGrpc.EchoStub echoStub;

    private Random random = new Random();

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public SharingClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        greeterStub1 = GreeterGrpc.newBlockingStub(channel);
        greeterStub2 = GreeterGrpc.newBlockingStub(channel);
        echoStub = EchoGrpc.newStub(channel);
    }

    /**
     * Say hello to server.
     */
    private void greet(String name, GreeterGrpc.GreeterBlockingStub stub, String stubName)
            throws InterruptedException {
        logger.info("Will try to greet {} using {}", name, stubName);
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = stub.sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return;
        }
        logger.info("Greeting: {}", response.getMessage());
        // pause to allow interleaving
        Thread.sleep(1000);
    }

    public void greet1(String name) throws InterruptedException {
        greet(name, greeterStub1, "greeter #1");
    }

    public void greet2(String name) throws InterruptedException {
        greet(name, greeterStub2, "greeter #2");
    }

    public StreamingFuture<List<String>> initiateEchos(List<String> valuesToSend) {
        StreamingFuture<List<String>> future = new StreamingFuture<List<String>>();
        List<String> valuesReceived = new ArrayList<>();

        // The logic that gets called by the framework during the RPC's lifecycle
        StreamObserver<EchoResponse> responseObserver = new StreamObserver<EchoResponse>() {
            @Override
            public void onNext(EchoResponse response) {
                logger.info("Received an echo: {}", response.getMessage());
                valuesReceived.add(response.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("Echo Failed: {}", Status.fromThrowable(t));
                future.setException(t);
            }

            @Override
            public void onCompleted() {
                logger.info("Server acknowledged end of echo stream.");
                future.set(valuesReceived);
            }
        };

        future.setObserver(responseObserver);

        new Thread(new Runnable() {
            public void run() {
                StreamObserver<EchoRequest> requestObserver =
                        echoStub.bidirectionalStreamingEcho(responseObserver);

                try {
                    for (String curValue : valuesToSend) {
                        logger.info("Sending an echo request for: {}", curValue);
                        EchoRequest req = EchoRequest.newBuilder().setMessage(curValue).build();
                        requestObserver.onNext(req);

                        // Sleep for a bit before sending the next one.
                        Thread.sleep(random.nextInt(1000) + 500);
                    }
                } catch (RuntimeException e) {
                    // Cancel RPC
                    requestObserver.onError(e);
                    throw e;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    requestObserver.onError(e);
                    return;
                }

                // Mark the end of requests
                requestObserver.onCompleted();
            }
        }).start();

        return future;
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     * You can see the multiplexing in the server logs.
     */
    public static void main(String[] args) throws Exception {
        String user = "world";
        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";
        // Allow passing in the user and target strings as command line arguments
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                logger.info("Usage: [name [target]]");
                logger.info("");
                logger.info("  name    The name you wish to be greeted by. Defaults to {}", user);
                logger.info("  target  The server to connect to. Defaults to {}", target);
                System.exit(1);
            }
            user = args[0];
        }
        if (args.length > 1) {
            target = args[1];
        }

        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        //
        // For the example we use plaintext insecure credentials to avoid needing TLS certificates. To
        // use TLS, use TlsChannelCredentials instead.
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        List<String> echoInput = ImmutableList.of("some", "thing", "wicked", "this", "way", "comes");
        try {
            SharingClient client = new SharingClient(channel);

            StreamingFuture<List<String>> future = client.initiateEchos(echoInput);
            client.greet1(user + " the great");
            client.greet2(user + " the lesser");
            client.greet1(user + " the humble");
            // Receiving happens asynchronously

            String resultStr = future.get(1, TimeUnit.MINUTES).toString();
            logger.info("The echo requests and results were:");
            logger.info("{}", echoInput.toString());
            logger.info("{}", resultStr);

            if (!future.isDone()) {
                logger.warn("Streaming rpc failed to complete in 1 minute");
            }
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private class StreamingFuture<RespT> extends AbstractFuture<RespT> {

        private StreamObserver<EchoResponse> responseObserver = null;

        private void setObserver(StreamObserver<EchoResponse> responseObserver) {
            this.responseObserver = responseObserver;
        }

        @Override
        protected void interruptTask() {
            if (responseObserver != null) {
                responseObserver.onError(Status.ABORTED.asException());
            }

        }

        // These are needed for visibility from the parent object
        @Override
        protected boolean set(@Nullable RespT resp) {
            return super.set(resp);
        }

        @Override
        protected boolean setException(Throwable throwable) {
            return super.setException(throwable);
        }

    }
}