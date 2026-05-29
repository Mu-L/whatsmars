package org.hongxi.whatsmars.grpc.server.deadline;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
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

public class DeadlineServer {
    private static final Logger logger = LoggerFactory.getLogger(DeadlineServer.class);

    private Server server;


    private void start() throws IOException {
        int port = 50051;
        SlowGreeter slowGreeter = new SlowGreeter();
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(slowGreeter)
                .build()
                .start();
        logger.info("Server started, listening on {}", port);

        // Create a channel to this same server so we can make a recursive call to demonstrate deadline
        // propagation.
        String target = "localhost:50051";
        slowGreeter.setClientStub(GreeterGrpc.newBlockingStub(
                Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build()));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    DeadlineServer.this.stop();
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
                "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");

        final DeadlineServer server = new DeadlineServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class SlowGreeter extends GreeterGrpc.GreeterImplBase {
        private GreeterGrpc.GreeterBlockingStub clientStub;

        void setClientStub(GreeterGrpc.GreeterBlockingStub clientStub) {
            this.clientStub = clientStub;
        }

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            if (req.getName().contains("propagate")) {
                clientStub.sayHello(HelloRequest.newBuilder().setName("Server").build());
            }

            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}