package org.hongxi.whatsmars.arthas.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 演示 Arthas 各类诊断命令的 Service
 *
 * <p>对应 Arthas 命令：
 * <ul>
 *   <li>{@code watch}   - 观察方法入参、返回值、异常</li>
 *   <li>{@code trace}   - 追踪方法内部调用链路耗时</li>
 *   <li>{@code stack}   - 打印方法被调用的调用链</li>
 *   <li>{@code thread}  - 查看线程信息（含死循环演示）</li>
 *   <li>{@code sc/sm}   - 查看类/方法元信息</li>
 *   <li>{@code jad}     - 反编译类</li>
 *   <li>{@code ognl}    - 调用静态方法、查看 Spring 上下文</li>
 * </ul>
 */
@Service
public class DiagnoseService {

    // ===================== watch 演示 =====================

    /**
     * 根据 id 查询用户，可能返回 null 或抛异常，适合用 watch 观察入参和返回值
     */
    public Map<String, Object> findUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("id must be positive, but got: " + id);
        }
        if (id > 1000) {
            return null; // 模拟查不到
        }
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", "user-" + id);
        user.put("email", "user" + id + "@example.com");
        return user;
    }

    // ===================== trace 演示 =====================

    /**
     * 模拟一个多步骤的业务流程，内部调用多个方法，
     * 用 trace 可以清晰看到每一步的耗时分布
     */
    public String processOrder(Long orderId) {
        validateOrder(orderId);
        String inventoryResult = checkInventory(orderId);
        double price = calculatePrice(orderId);
        String paymentResult = processPayment(orderId, price);
        return "Order " + orderId + " processed: " + paymentResult;
    }

    private void validateOrder(Long orderId) {
        sleep(50);
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("invalid orderId: " + orderId);
        }
    }

    private String checkInventory(Long orderId) {
        sleep(120);
        return "in-stock";
    }

    private double calculatePrice(Long orderId) {
        sleep(30);
        return ThreadLocalRandom.current().nextDouble(10, 500);
    }

    private String processPayment(Long orderId, double amount) {
        sleep(200);
        return "paid-" + String.format("%.2f", amount);
    }

    // ===================== stack 演示 =====================

    /**
     * 用 stack 命令可以查看该方法是被谁调用的
     */
    public void logAction(String action) {
        System.out.println("[DiagnoseService] action=" + action
                + " thread=" + Thread.currentThread().getName());
    }

    public void triggerLogFromA() {
        logAction("from-A");
    }

    public void triggerLogFromB() {
        triggerLogFromBInner();
    }

    private void triggerLogFromBInner() {
        logAction("from-B");
    }

    // ===================== thread 演示 =====================

    /**
     * 模拟 CPU 密集计算，thread 命令可看到该线程 CPU 占用高
     */
    public long busyLoop(long iterations) {
        long sum = 0;
        for (long i = 0; i < iterations; i++) {
            sum += (long) Math.sqrt(i) * (long) Math.cbrt(i);
        }
        return sum;
    }

    // ===================== sc / sm / jad 演示 =====================

    /**
     * 用 sc 查看类信息，sm 查看方法列表，jad 反编译该类
     */
    public String getClassInfo() {
        return "class=" + this.getClass().getName()
                + " classLoader=" + this.getClass().getClassLoader();
    }

    // ===================== ognl 演示 =====================

    /**
     * 供 ognl 命令调用，演示如何在不修改代码的情况下执行任意逻辑
     */
    public static String echo(String message) {
        return "[echo] " + message;
    }

    public static List<String> generateList(int size) {
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add("item-" + i);
        }
        return list;
    }

    // ===================== 工具方法 =====================

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
