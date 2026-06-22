package org.hongxi.whatsmars.internal.concurrent;

import java.util.concurrent.atomic.*;

/**
 * 原子类示例：CAS操作、AtomicInteger/AtomicReference/AtomicStampedReference/LongAdder
 */
class AtomicExamples {

    public static void main(String[] args) throws Exception {
        atomicIntegerDemo();
        atomicReferenceDemo();
        atomicStampedReferenceDemo();
        longAdderDemo();
    }

    // ==================== 1. AtomicInteger ====================

    static void atomicIntegerDemo() throws Exception {
        System.out.println("===== AtomicInteger =====");

        AtomicInteger ai = new AtomicInteger(0);

        // 基本操作
        ai.set(10);
        System.out.println("set(10): " + ai.get());

        // CAS 操作
        boolean swapped = ai.compareAndSet(10, 20);
        System.out.println("CAS(10->20): " + swapped + ", 当前值=" + ai.get());

        boolean failed = ai.compareAndSet(10, 30); // 期望值不匹配
        System.out.println("CAS(10->30): " + failed + ", 当前值=" + ai.get());

        // 原子自增（等同于 i++ 但线程安全）
        System.out.println("incrementAndGet: " + ai.incrementAndGet());
        System.out.println("getAndIncrement: " + ai.getAndIncrement());
        System.out.println("addAndGet(10): " + ai.addAndGet(10));

        // 多线程并发自增
        AtomicInteger counter = new AtomicInteger(0);
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    counter.incrementAndGet();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("并发自增结果(期望100000): " + counter.get());
        System.out.println();
    }

    // ==================== 2. AtomicReference ====================

    static void atomicReferenceDemo() {
        System.out.println("===== AtomicReference =====");

        // 原子更新引用类型
        AtomicReference<String> ref = new AtomicReference<>("hello");
        boolean updated = ref.compareAndSet("hello", "world");
        System.out.println("CAS更新引用: " + updated + ", 值=" + ref.get());

        // AtomicMarkableReference: 带布尔标记的引用
        AtomicMarkableReference<String> marked = new AtomicMarkableReference<>("data", false);
        marked.set("new-data", true);
        System.out.println("MarkableReference: value=" + marked.getReference() + ", mark=" + marked.isMarked());

        System.out.println();
    }

    // ==================== 3. AtomicStampedReference 解决ABA问题 ====================

    static void atomicStampedReferenceDemo() {
        System.out.println("===== AtomicStampedReference (解决ABA) =====");

        // ABA问题：值从A变成B又变回A，普通CAS无法感知
        // AtomicStampedReference 通过版本号(时间戳)解决

        int initialRef = 100;
        int initialStamp = 0;
        AtomicStampedReference<Integer> asr = new AtomicStampedReference<>(initialRef, initialStamp);

        int[] stampHolder = new int[1];
        int currentRef = asr.get(stampHolder);
        int currentStamp = stampHolder[0];
        System.out.println("初始值: ref=" + currentRef + ", stamp=" + currentStamp);

        // 模拟 A -> B -> A 的变化
        asr.compareAndSet(100, 200, currentStamp, currentStamp + 1); // A->B
        asr.compareAndSet(200, 100, currentStamp + 1, currentStamp + 2); // B->A

        // 此时值回到100，但stamp已变化
        boolean result = asr.compareAndSet(100, 300, currentStamp, currentStamp + 1);
        // 失败！因为stamp已不是currentStamp
        System.out.println("用旧stamp CAS(100->300): " + result + " (ABA保护生效)");

        // 使用最新stamp才能成功
        int[] latestStamp = new int[1];
        int latestRef = asr.get(latestStamp);
        result = asr.compareAndSet(latestRef, 300, latestStamp[0], latestStamp[0] + 1);
        System.out.println("用最新stamp CAS(100->300): " + result);
        System.out.println();
    }

    // ==================== 4. LongAdder（高并发计数器）====================

    static void longAdderDemo() throws Exception {
        System.out.println("===== LongAdder =====");

        // LongAdder vs AtomicLong:
        // - 低并发时 AtomicLong 性能更好
        // - 高并发时 LongAdder 通过分段累加减少竞争，性能远超 AtomicLong
        // - LongAdder 的 sum() 是最终一致性（非强一致）

        LongAdder adder = new LongAdder();
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10000; j++) {
                    adder.increment();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("LongAdder结果(期望100000): " + adder.sum());

        // LongAccumulator: 更通用的累加器，可自定义累加函数
        LongAccumulator maxAccumulator = new LongAccumulator(Long::max, 0);
        Thread[] ts = new Thread[5];
        for (int i = 0; i < ts.length; i++) {
            final int val = i + 1;
            ts[i] = new Thread(() -> maxAccumulator.accumulate(val));
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        System.out.println("LongAccumulator最大值: " + maxAccumulator.get());
        System.out.println();
    }
}
