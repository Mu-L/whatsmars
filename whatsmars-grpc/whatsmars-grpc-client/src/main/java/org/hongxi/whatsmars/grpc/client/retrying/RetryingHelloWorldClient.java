package org.hongxi.whatsmars.grpc.client.retrying;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A client that requests a greeting from the RetryingHelloWorldServer with a retrying policy.
 */
public class RetryingHelloWorldClient {
    private static final Logger logger = LoggerFactory.getLogger(RetryingHelloWorldClient.class);

    static final String ENV_DISABLE_RETRYING = "DISABLE_RETRYING_IN_RETRYING_EXAMPLE";

    private final boolean enableRetries;
    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final AtomicInteger totalRpcs = new AtomicInteger();
    private final AtomicInteger failedRpcs = new AtomicInteger();

    protected Map<String, ?> getRetryingServiceConfig() {
        return new Gson()
                .fromJson(
                        new JsonReader(
                                new InputStreamReader(
                                        RetryingHelloWorldClient.class.getResourceAsStream(
                                                "/retrying/retrying_service_config.json"),
                                        UTF_8)),
                        Map.class);
    }

    /**
     * Construct client connecting to HelloWorld server at {@code host:port}.
     */
    public RetryingHelloWorldClient(String host, int port, boolean enableRetries) {

        ManagedChannelBuilder<?> channelBuilder
                = Grpc.newChannelBuilderForAddress(host, port, InsecureChannelCredentials.create());
        if (enableRetries) {
            Map<String, ?> serviceConfig = getRetryingServiceConfig();
            logger.info("Client started with retrying configuration: " + serviceConfig);
            channelBuilder.defaultServiceConfig(serviceConfig).enableRetry();
        }
        channel = channelBuilder.build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
        this.enableRetries = enableRetries;
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(60, TimeUnit.SECONDS);
    }

    /**
     * Say hello to server in a blocking unary call.
     */
    public void greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response = null;
        StatusRuntimeException statusRuntimeException = null;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            failedRpcs.incrementAndGet();
            statusRuntimeException = e;
        }

        totalRpcs.incrementAndGet();

        if (statusRuntimeException == null) {
            logger.info("Greeting: {}", response.getMessage());
        } else {
            logger.info("RPC failed: {}", statusRuntimeException.getStatus());
        }
    }

    private void printSummary() {
        logger.info("\n\nTotal RPCs sent: {}. Total RPCs failed: {}\n", totalRpcs.get(), failedRpcs.get());

        if (enableRetries) {
            logger.info("Retrying enabled. To disable retries, run the client with environment variable {}=true.", ENV_DISABLE_RETRYING);
        } else {
            logger.info("Retrying disabled. To enable retries, unset environment variable {} and then run the client.", ENV_DISABLE_RETRYING);
        }
    }

    public static void main(String[] args) throws Exception {
        boolean enableRetries = !Boolean.parseBoolean(System.getenv(ENV_DISABLE_RETRYING));
        final RetryingHelloWorldClient client = new RetryingHelloWorldClient("localhost", 50051, enableRetries);
        ForkJoinPool executor = new ForkJoinPool();

        for (int i = 0; i < 50; i++) {
            final String userId = "user" + i;
            executor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            client.greet(userId);
                        }
                    });
        }
        executor.awaitQuiescence(100, TimeUnit.SECONDS);
        executor.shutdown();
        client.printSummary();
        client.shutdown();
    }
}