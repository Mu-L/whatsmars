package org.hongxi.whatsmars.grpc.server.header;

import com.google.common.annotations.VisibleForTesting;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A interceptor to handle server header.
 */
public class HeaderServerInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HeaderServerInterceptor.class);

    @VisibleForTesting
    static final Metadata.Key<String> CUSTOM_HEADER_KEY =
            Metadata.Key.of("custom_server_header_key", Metadata.ASCII_STRING_MARSHALLER);


    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            final Metadata requestHeaders,
            ServerCallHandler<ReqT, RespT> next) {
        logger.info("header received from client:{}", requestHeaders);
        return next.startCall(new SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata responseHeaders) {
                responseHeaders.put(CUSTOM_HEADER_KEY, "customRespondValue");
                super.sendHeaders(responseHeaders);
            }
        }, requestHeaders);
    }
}