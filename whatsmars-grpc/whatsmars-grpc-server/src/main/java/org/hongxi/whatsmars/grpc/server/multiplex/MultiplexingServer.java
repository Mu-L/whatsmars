package org.hongxi.whatsmars.grpc.server.multiplex;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A sample gRPC server that serves both the Greeting and Echo services.
 */
public class MultiplexingServer {

    private static final Logger logger = LoggerFactory.getLogger(MultiplexingServer.class);

    private final int port;
    private Server server;

    public MultiplexingServer(int port) throws IOException {
        this.port = port;
    }

    private void start() throws IOException {
        /* The port on which the server should run */
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new MultiplexingServer.GreeterImpl())
                .addService(new EchoService())
                .build()
                .start();
        logger.info("Server started, listening on {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    MultiplexingServer.this.stop();
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
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tH:%1$tM:%1$tS %4$s %2$s: %5$s%6$s%n");

        final MultiplexingServer server = new MultiplexingServer(50051);
        server.start();
        server.blockUntilShutdown();
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            logger.info("Received sayHello request: {}", req.getName());
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}