package org.hongxi.whatsmars.internal.concurrent;

import java.util.concurrent.*;

/**
 * 同步工具示例：CountDownLatch、CyclicBarrier、Semaphore、Exchanger
 */
class SyncAidExamples {

    public static void main(String[] args) throws Exception {
        countDownLatchDemo();
        cyclicBarrierDemo();
        semaphoreDemo();
    }

    // ==================== 1. CountDownLatch（一次性门闩）====================

    static void countDownLatchDemo() throws Exception {
        System.out.println("===== CountDownLatch =====");

        // 场景：主线程等待多个子任务全部完成后汇总结果
        // 计数器只能递减，不能重置（一次性使用）

        int taskCount = 3;
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    Thread.sleep((id + 1) * 100);
                    System.out.println("子任务-" + id + " 完成");
                } catch (InterruptedException ignored) {
                } finally {
                    latch.countDown(); // 计数减1
                }
            }).start();
        }

        // 方式一：阻塞等待所有任务完成
        latch.await();
        System.out.println("所有子任务完成，主线程汇总");

        // 方式二：带超时的等待
        // boolean finished = latch.await(2, TimeUnit.SECONDS);

        // 方式三：等待指定时间后检查剩余计数
        // long remaining = latch.getCount();
        System.out.println();
    }

    // ==================== 2. CyclicBarrier（可循环屏障）====================

    static void cyclicBarrierDemo() throws Exception {
        System.out.println("===== CyclicBarrier =====");

        // 场景：N个线程互相等待，全部到达屏障后一起执行
        // 可重复使用（reset/reuse），适合多阶段算法

        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties, () ->
            System.out.println(">> 屏障动作：所有线程到达，执行汇总逻辑")
        );

        for (int i = 0; i < parties; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("线程-" + id + " 开始第一阶段");
                    Thread.sleep((id + 1) * 50);

                    barrier.await(); // 等待所有线程到达

                    System.out.println("线程-" + id + " 开始第二阶段");
                    Thread.sleep(50);

                    barrier.await(); // 再次等待
                    System.out.println("线程-" + id + " 全部完成");
                } catch (InterruptedException | BrokenBarrierException e) {
                    System.out.println("线程-" + id + " 异常: " + e.getMessage());
                }
            }).start();
        }

        Thread.sleep(1000); // 等待演示完成

        // CyclicBarrier vs CountDownLatch:
        // - CountDownLatch: 一个/多个线程等待其他线程完成，一次性
        // - CyclicBarrier:  N个线程互相等待，可重用
        System.out.println();
    }

    // ==================== 3. Semaphore（信号量）====================

    static void semaphoreDemo() throws Exception {
        System.out.println("===== Semaphore =====");

        // 场景：限制同时访问某资源的线程数（如限流、连接池）
        // 公平模式：按FIFO顺序获取许可

        int permits = 2; // 最多允许2个线程同时访问
        Semaphore semaphore = new Semaphore(permits);

        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                try {
                    semaphore.acquire(); // 获取许可，无许可则阻塞
                    System.out.println("线程-" + id + " 获取许可, 剩余可用=" + semaphore.availablePermits());
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                } finally {
                    semaphore.release(); // 释放许可
                    System.out.println("线程-" + id + " 释放许可");
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        // tryAcquire: 非阻塞尝试
        if (semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
            try {
                System.out.println("tryAcquire 成功");
            } finally {
                semaphore.release();
            }
        }
        System.out.println();
    }
}
