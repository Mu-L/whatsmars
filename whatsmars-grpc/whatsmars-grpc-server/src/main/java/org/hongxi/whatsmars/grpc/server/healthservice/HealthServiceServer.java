package org.hongxi.whatsmars.grpc.server.healthservice;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.Status;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class HealthServiceServer {
    private static final Logger logger = LoggerFactory.getLogger(HealthServiceServer.class);

    private Server server;
    private HealthStatusManager health;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        health = new HealthStatusManager();
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new GreeterImpl())
                .addService(health.getHealthService())
                .build()
                .start();
        logger.info("Server started, listening on {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                logger.info("Shutting down gRPC server since JVM is shutting down");
                try {
                    HealthServiceServer.this.stop();
                } catch (InterruptedException e) {
                    logger.error("Error during server shutdown", e);
                }
                logger.info("Server shut down");
            }
        });

        health.setStatus("", ServingStatus.SERVING);
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
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tH:%1$tM:%1$tS %4$s %2$s: %5$s%6$s%n");

        final HealthServiceServer server = new HealthServiceServer();
        server.start();
        server.blockUntilShutdown();
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {
        private volatile boolean isServing = true;

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            if (!isServing) {
                responseObserver.onError(
                        Status.INTERNAL.withDescription("Not Serving right now").asRuntimeException());
                return;
            }

            if (isNameLongEnough(req)) {
                HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            } else {
                logger.warn("Tiny message received, throwing a temper tantrum");
                health.setStatus("", ServingStatus.NOT_SERVING);
                isServing = false;

                // In 10 seconds set it back to serving
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        isServing = true;
                        health.setStatus("", ServingStatus.SERVING);
                        logger.info("tantrum complete");
                    }
                }).start();
                responseObserver.onError(
                        Status.INVALID_ARGUMENT.withDescription("Offended by short name").asRuntimeException());
            }
        }

        private boolean isNameLongEnough(HelloRequest req) {
            return req.getName().length() >= 5;
        }
    }
}