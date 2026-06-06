package org.hongxi.whatsmars.spring.aspect.demo;

import org.hongxi.whatsmars.spring.aspect.Monitor;
import org.springframework.stereotype.Service;

/**
 * Demonstrates various AOP interception scenarios via {@link Monitor} annotation.
 */
@Service
public class DemoService {

    @Monitor(tag = "demo.greet")
    public String greet(String name) {
        System.out.println("  [DemoService] greet() executing...");
        return "Hello, " + name + "!";
    }

    @Monitor(tag = "demo.compute")
    public int compute(int a, int b) {
        System.out.println("  [DemoService] compute() executing...");
        return a + b;
    }

    @Monitor(tag = "demo.fail")
    public void fail() {
        System.out.println("  [DemoService] fail() about to throw...");
        throw new RuntimeException("simulated failure");
    }
}
