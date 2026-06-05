package org.hongxi.whatsmars.grpc.server.header;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A simple server that like HelloWorldServer.
 * You can get and response any header in {@link org.hongxi.whatsmars.grpc.server.header.HeaderServerInterceptor}.
 */
public class CustomHeaderServer {
  private static final Logger logger = LoggerFactory.getLogger(CustomHeaderServer.class);

  /* The port on which the server should run */
  private static final int PORT = 50051;
  private Server server;

  private void start() throws IOException {
    server = Grpc.newServerBuilderForPort(PORT, InsecureServerCredentials.create())
        .addService(ServerInterceptors.intercept(new GreeterImpl(), new HeaderServerInterceptor()))
        .build()
        .start();
    logger.info("Server started, listening on {}", PORT);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        logger.info("Shutting down gRPC server since JVM is shutting down");
        try {
          CustomHeaderServer.this.stop();
        } catch (InterruptedException e) {
          logger.error("Error during server shutdown", e);
        }
        logger.info("Server shut down");
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
    final CustomHeaderServer server = new CustomHeaderServer();
    server.start();
    server.blockUntilShutdown();
  }

  private static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
      HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }
  }
}