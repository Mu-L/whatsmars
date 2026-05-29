package org.hongxi.whatsmars.grpc.server.preserialized;

import io.grpc.BindableService;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Server that provides a {@code Greeter} service, but that uses a pre-serialized response. This is
 * a performance optimization that can be useful if you read the response from on-disk or a database
 * where it is already serialized, or if you need to send the same complicated message to many
 * clients. The same approach can avoid deserializing requests, to be stored in a database. This
 * adjustment is server-side only; the client is unable to detect the differences, so this server is
 * fully-compatible with the normal HelloWorldClient.
 */
public class PreSerializedServer {
    private static final Logger logger = LoggerFactory.getLogger(PreSerializedServer.class);

    private Server server;

    private void start() throws IOException {
        int port = 50051;
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new GreeterImpl())
                .build()
                .start();
        logger.info("Server started, listening on {}", port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    PreSerializedServer.this.stop();
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
        final PreSerializedServer server = new PreSerializedServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class GreeterImpl implements GreeterGrpc.AsyncService, BindableService {

        public void byteSayHello(HelloRequest req, StreamObserver<byte[]> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply.toByteArray());
            responseObserver.onCompleted();
        }

        @Override
        public ServerServiceDefinition bindService() {
            MethodDescriptor<HelloRequest, HelloReply> sayHello = GreeterGrpc.getSayHelloMethod();
            // Modifying the method descriptor to use bytes as the response, instead of HelloReply. By
            // adjusting toBuilder() you can choose which of the request and response are bytes.
            MethodDescriptor<HelloRequest, byte[]> byteSayHello = sayHello
                    .toBuilder(sayHello.getRequestMarshaller(), new ByteArrayMarshaller())
                    .build();
            // GreeterGrpc.bindService() will bind every service method, including sayHello(). (Although
            // Greeter only has one method, this approach would work for any service.) AsyncService
            // provides a default implementation of sayHello() that returns UNIMPLEMENTED, and that
            // implementation will be used by bindService(). replaceMethod() will rewrite that method to
            // use our byte-based method instead.
            //
            // The generated bindService() uses ServerCalls to make RPC handlers. Since the generated
            // bindService() won't expect byte[] in the AsyncService, this uses ServerCalls directly. It
            // isn't as convenient, but it behaves the same as a normal RPC handler.
            return replaceMethod(
                    GreeterGrpc.bindService(this),
                    byteSayHello,
                    ServerCalls.asyncUnaryCall(this::byteSayHello));
        }

        /**
         * Rewrites the ServerServiceDefinition replacing one method's definition.
         */
        private static <ReqT, RespT> ServerServiceDefinition replaceMethod(
                ServerServiceDefinition def,
                MethodDescriptor<ReqT, RespT> newDesc,
                ServerCallHandler<ReqT, RespT> newHandler) {
            // There are two data structures involved. The first is the "descriptor" which describes the
            // service and methods as a schema. This is the same on client and server. The second is the
            // "definition" which includes the handlers to execute methods. This is specific to the server
            // and is generated by "bind." This adjusts both the descriptor and definition.

            // Descriptor
            ServiceDescriptor desc = def.getServiceDescriptor();
            ServiceDescriptor.Builder descBuilder = ServiceDescriptor.newBuilder(desc.getName())
                    .setSchemaDescriptor(desc.getSchemaDescriptor())
                    .addMethod(newDesc); // Add the modified method
            // Copy methods other than the modified one
            for (MethodDescriptor<?, ?> md : desc.getMethods()) {
                if (newDesc.getFullMethodName().equals(md.getFullMethodName())) {
                    continue;
                }
                descBuilder.addMethod(md);
            }

            // Definition
            ServerServiceDefinition.Builder defBuilder =
                    ServerServiceDefinition.builder(descBuilder.build())
                            .addMethod(newDesc, newHandler); // Add the modified method
            // Copy methods other than the modified one
            for (ServerMethodDefinition<?, ?> smd : def.getMethods()) {
                if (newDesc.getFullMethodName().equals(smd.getMethodDescriptor().getFullMethodName())) {
                    continue;
                }
                defBuilder.addMethod(smd);
            }
            return defBuilder.build();
        }
    }
}