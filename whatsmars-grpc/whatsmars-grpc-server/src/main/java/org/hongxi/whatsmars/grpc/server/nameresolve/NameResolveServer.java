package org.hongxi.whatsmars.grpc.server.nameresolve;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NameResolveServer {
    private static final Logger logger = LoggerFactory.getLogger(NameResolveServer.class);

    static public final int serverCount = 3;
    static public final int startPort = 50051;
    private Server[] servers;

    public static void main(String[] args) throws IOException, InterruptedException {
        final NameResolveServer server = new NameResolveServer();
        server.start();
        server.blockUntilShutdown();
    }

    private void start() throws IOException {
        servers = new Server[serverCount];
        for (int i = 0; i < serverCount; i++) {
            int port = startPort + i;
            servers[i] = ServerBuilder.forPort(port)
                    .addService(new GreeterImpl(port))
                    .build()
                    .start();
            logger.info("Server started, listening on {}", port);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                NameResolveServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    private void stop() throws InterruptedException {
        for (int i = 0; i < serverCount; i++) {
            if (servers[i] != null) {
                servers[i].shutdown().awaitTermination(30, TimeUnit.SECONDS);
            }
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        for (int i = 0; i < serverCount; i++) {
            if (servers[i] != null) {
                servers[i].awaitTermination();
            }
        }
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        int port;

        public GreeterImpl(int port) {
            this.port = port;
        }

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName() + " from server<" + this.port + ">").build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}