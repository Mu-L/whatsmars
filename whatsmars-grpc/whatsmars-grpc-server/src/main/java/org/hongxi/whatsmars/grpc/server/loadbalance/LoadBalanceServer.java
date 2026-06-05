package org.hongxi.whatsmars.grpc.server.loadbalance;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoadBalanceServer {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalanceServer.class);

    static final int[] SERVER_PORTS = {50051, 50052, 50053};
    private List<Server> servers;

    private void start() throws IOException {
        servers = new ArrayList<>();
        for (int port : SERVER_PORTS) {
            servers.add(
                ServerBuilder.forPort(port)
                    .addService(new GreeterImpl(port))
                    .build()
                    .start());
            logger.info("Server started, listening on {}", port);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server since JVM is shutting down");
            try {
                LoadBalanceServer.this.stop();
            } catch (InterruptedException e) {
                logger.error("Error during server shutdown", e);
            }
            logger.info("Server shut down");
        }));
    }

    private void stop() throws InterruptedException {
        for (Server server : servers) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        for (Server server : servers) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final LoadBalanceServer server = new LoadBalanceServer();
        server.start();
        server.blockUntilShutdown();
    }

}