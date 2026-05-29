package org.hongxi.whatsmars.grpc.server.multiplex;

import org.hongxi.whatsmars.grpc.api.echo.EchoGrpc;
import org.hongxi.whatsmars.grpc.api.echo.EchoRequest;
import org.hongxi.whatsmars.grpc.api.echo.EchoResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service that echoes back whatever is sent to it.
 */
public class EchoService extends EchoGrpc.EchoImplBase {
    private static final Logger logger = LoggerFactory.getLogger(EchoService.class);

    @Override
    public void unaryEcho(EchoRequest request,
                          StreamObserver<EchoResponse> responseObserver) {
        logger.info("Received echo request: {}", request.getMessage());
        EchoResponse response = EchoResponse.newBuilder().setMessage(request.getMessage()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void serverStreamingEcho(EchoRequest request,
                                    StreamObserver<EchoResponse> responseObserver) {
        logger.info("Received server streaming echo request: {}", request.getMessage());
        EchoResponse response = EchoResponse.newBuilder().setMessage(request.getMessage()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<EchoRequest> clientStreamingEcho(
            final StreamObserver<EchoResponse> responseObserver) {
        return new StreamObserver<EchoRequest>() {
            List<String> requestList = new ArrayList<>();

            @Override
            public void onNext(EchoRequest request) {
                logger.info("Received client streaming echo request: " + request.getMessage());
                requestList.add(request.getMessage());
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("echo stream cancelled or had a problem and is no longer usable - {}", t.getMessage());
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                logger.info("Client streaming complete");
                String reply = requestList.stream().collect(Collectors.joining(", "));
                EchoResponse response = EchoResponse.newBuilder().setMessage(reply).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<EchoRequest> bidirectionalStreamingEcho(
            final StreamObserver<EchoResponse> responseObserver) {
        return new StreamObserver<EchoRequest>() {
            @Override
            public void onNext(EchoRequest request) {
                logger.info("Received bidirection streaming echo request: {}", request.getMessage());
                EchoResponse response = EchoResponse.newBuilder().setMessage(request.getMessage()).build();
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                logger.warn("echo stream cancelled or had a problem and is no longer usable - {}", t.getMessage());
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                logger.info("Bidirectional stream completed from client side");
                responseObserver.onCompleted();
            }
        };
    }
}
