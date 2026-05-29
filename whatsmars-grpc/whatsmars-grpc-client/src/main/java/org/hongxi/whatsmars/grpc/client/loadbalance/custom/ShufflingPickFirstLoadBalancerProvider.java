package org.hongxi.whatsmars.grpc.client.loadbalance.custom;

import io.grpc.LoadBalancer;
import io.grpc.LoadBalancer.Helper;
import io.grpc.LoadBalancerProvider;
import io.grpc.NameResolver.ConfigOrError;
import io.grpc.Status;

import java.util.Map;

public class ShufflingPickFirstLoadBalancerProvider extends LoadBalancerProvider {

    @Override
    public ConfigOrError parseLoadBalancingPolicyConfig(Map<String, ?> rawLoadBalancingPolicyConfig) {
        Long randomSeed = null;

        // The load balancing configuration generally comes from a remote source over the wire, be
        // defensive when parsing it.
        try {
            Object randomSeedObj = rawLoadBalancingPolicyConfig.get("randomSeed");
            if (randomSeedObj instanceof Double) {
                randomSeed = ((Double) randomSeedObj).longValue();
            }
            return ConfigOrError.fromConfig(new ShufflingPickFirstLoadBalancer.Config(randomSeed));
        } catch (RuntimeException e) {
            return ConfigOrError.fromError(
                    Status.UNAVAILABLE.withDescription("unable to parse LB config").withCause(e));
        }
    }

    @Override
    public LoadBalancer newLoadBalancer(Helper helper) {
        return new ShufflingPickFirstLoadBalancer(helper);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public String getPolicyName() {
        return "grpc.examples.customloadbalance.ShufflingPickFirst";
    }
}