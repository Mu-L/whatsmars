package org.hongxi.whatsmars.grpc.server.loadbalance;

import io.grpc.stub.StreamObserver;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;

public class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        int port;

        public GreeterImpl(int port) {
            this.port = port;
        }

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello " + req.getName() + " from server<" + this.port + ">").build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }