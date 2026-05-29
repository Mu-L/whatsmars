package org.hongxi.whatsmars.grpc.client.loadbalance.custom;

import com.google.gson.Gson;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.LoadBalancerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.NameResolverRegistry;
import io.grpc.StatusRuntimeException;
import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloReply;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import org.hongxi.whatsmars.grpc.client.loadbalance.ExampleNameResolverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This client is intended for connecting with the {@code LoadBalanceServer} in the "loadbalance"
 * example.
 */
public class CustomLoadBalanceClient {

    private static final Logger logger = LoggerFactory.getLogger(CustomLoadBalanceClient.class);

    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    public CustomLoadBalanceClient(Channel channel) {
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.warn("RPC failed: {}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getMessage());
    }


    public static void main(String[] args) throws Exception {
        // We need to register the provider of our custom load balancer implementation
        LoadBalancerRegistry.getDefaultRegistry()
                .register(new ShufflingPickFirstLoadBalancerProvider());

        NameResolverRegistry.getDefaultRegistry().register(new ExampleNameResolverProvider());

        String target = "example:///lb.example.grpc.io";

        logger.info("Use default first_pick load balance policy");
        ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();

        try {
            CustomLoadBalanceClient client = new CustomLoadBalanceClient(channel);
            for (int i = 0; i < 5; i++) {
                client.greet("request" + i);
            }
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }

        logger.info("Change to custom shuffling_pick_first policy with a configured random seed");
        // The load balancer name in the config needs to match what getPolicyName() in the provider
        // returns. The randomSeed field we are using also needs to be understood by the provider when
        // parseLoadBalancingPolicyConfig() gets called.
        Map<String, ?> serviceConfig = new Gson().fromJson(
                "{ \"loadBalancingConfig\": " +
                        "    [ { \"grpc.examples.customloadbalance.ShufflingPickFirst\": { \"randomSeed\": 123 } } ]" +
                        "}",
                Map.class);
        channel = ManagedChannelBuilder.forTarget(target)
                .defaultServiceConfig(serviceConfig)
                .usePlaintext()
                .build();
        try {
            CustomLoadBalanceClient client = new CustomLoadBalanceClient(channel);
            for (int i = 0; i < 5; i++) {
                client.greet("request" + i);
            }
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}