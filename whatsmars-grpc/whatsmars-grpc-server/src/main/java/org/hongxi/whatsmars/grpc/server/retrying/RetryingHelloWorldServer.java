package org.hongxi.whatsmars.grpc.server.retrying;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.Status;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A HelloWorld server that responds to requests with UNAVAILABLE with a given percentage.
 */
public class RetryingHelloWorldServer {
    private static final Logger logger = LoggerFactory.getLogger(RetryingHelloWorldServer.class);

    private static final float UNAVAILABLE_PERCENTAGE = 0.5F;
    private static final Random random = new Random();

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new GreeterImpl())
                .build()
                .start();
        logger.info("Server started, listening on {}", port);

        DecimalFormat df = new DecimalFormat("#%");
        logger.info("Responding as UNAVAILABLE to {} requests", df.format(UNAVAILABLE_PERCENTAGE));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    RetryingHelloWorldServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final RetryingHelloWorldServer server = new RetryingHelloWorldServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        AtomicInteger retryCounter = new AtomicInteger(0);

        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            int count = retryCounter.incrementAndGet();
            if (random.nextFloat() < UNAVAILABLE_PERCENTAGE) {
                logger.info("Returning stubbed UNAVAILABLE error. count: {}", count);
                responseObserver.onError(Status.UNAVAILABLE
                        .withDescription("Greeter temporarily unavailable...").asRuntimeException());
            } else {
                logger.info("Returning successful Hello response, count: {}", count);
                HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        }
    }
}