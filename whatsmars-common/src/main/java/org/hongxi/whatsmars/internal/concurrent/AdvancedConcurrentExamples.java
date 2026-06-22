package org.hongxi.whatsmars.internal.concurrent;

import java.util.*;
import java.util.concurrent.*;

/**
 * 高级并发工具补充示例：Phaser、Exchanger、ForkJoinPool、CompletionService、
 * ScheduledExecutorService、LinkedTransferQueue、ConcurrentSkipListMap
 */
class AdvancedConcurrentExamples {

    public static void main(String[] args) throws Exception {
        phaserDemo();
        exchangerDemo();
        forkJoinPoolDemo();
        completionServiceDemo();
        scheduledExecutorDemo();
        linkedTransferQueueDemo();
        concurrentSkipListDemo();
    }

    // ==================== 1. Phaser（灵活屏障，JDK 7+）====================

    static void phaserDemo() throws Exception {
        System.out.println("===== Phaser =====");

        // Phaser vs CyclicBarrier:
        // - CyclicBarrier: 参与者数量固定，一次性使用后可重置
        // - Phaser: 参与者数量可动态增减（register/deregister），支持多阶段

        Phaser phaser = new Phaser(1); // 初始注册1个（主线程自己）

        for (int i = 0; i < 3; i++) {
            final int id = i;
            phaser.register(); // 动态注册新参与者
            new Thread(() -> {
                // 阶段1
                System.out.println("线程-" + id + " 阶段1开始");
                try { Thread.sleep((id + 1) * 50); } catch (InterruptedException ignored) {}
                System.out.println("线程-" + id + " 阶段1完成，等待...");
                phaser.arriveAndAwaitAdvance(); // 到达并等待

                // 阶段2
                System.out.println("线程-" + id + " 阶段2开始");
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                phaser.arriveAndAwaitAdvance();
            }).start();
        }

        phaser.arriveAndDeregister(); // 主线程取消注册
        System.out.println("Phaser 当前阶段: " + phaser.getPhase());
        System.out.println("已注册参与者: " + phaser.getRegisteredParties());

        // 等待所有线程完成
        while (!phaser.isTerminated()) {
            Thread.sleep(50);
        }
        System.out.println();
    }

    // ==================== 2. Exchanger（线程间数据交换）====================

    static void exchangerDemo() throws Exception {
        System.out.println("===== Exchanger =====");

        // 两个线程在同步点交换数据
        // 典型场景：生产者生产数据 -> 消费者处理数据，交换空缓冲区/满缓冲区

        Exchanger<List<Integer>> exchanger = new Exchanger<>();

        // 生产者：填充数据后交换
        Thread producer = new Thread(() -> {
            try {
                List<Integer> buffer = new ArrayList<>();
                for (int i = 1; i <= 5; i++) {
                    buffer.add(i);
                }
                System.out.println("生产者填充: " + buffer);
                // 交换：把自己的满缓冲区给对方，拿到对方的空缓冲区
                List<Integer> received = exchanger.exchange(buffer, 2, TimeUnit.SECONDS);
                System.out.println("生产者收到: " + received);
            } catch (InterruptedException | TimeoutException e) {
                System.out.println("生产者异常: " + e.getMessage());
            }
        });

        // 消费者：准备空缓冲区来交换
        Thread consumer = new Thread(() -> {
            try {
                List<Integer> emptyBuffer = new ArrayList<>();
                System.out.println("消费者准备空缓冲区");
                // 交换：把空缓冲区给对方，拿到对方的满缓冲区
                List<Integer> received = exchanger.exchange(emptyBuffer, 2, TimeUnit.SECONDS);
                System.out.println("消费者收到: " + received + "，开始处理...");
            } catch (InterruptedException | TimeoutException e) {
                System.out.println("消费者异常: " + e.getMessage());
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        System.out.println();
    }

    // ==================== 3. ForkJoinPool（分治框架）====================

    static void forkJoinPoolDemo() throws Exception {
        System.out.println("===== ForkJoinPool =====");

        // 核心思想：分而治之 + 工作窃取（work-stealing）
        // - 大任务拆分为小任务（fork）
        // - 小任务结果合并（join）
        // - 空闲线程会从忙碌线程的队列尾部"偷"任务

        // 示例：并行计算 1~10000 的和
        ForkJoinPool pool = new ForkJoinPool();
        SumTask task = new SumTask(1, 10000);
        long result = pool.invoke(task);
        System.out.println("ForkJoin 1~10000求和: " + result + " (期望50005000)");
        System.out.println("并行度: " + pool.getParallelism());
        System.out.println("窃取任务数: " + pool.getStealCount());
        pool.shutdown();
        System.out.println();
    }

    static class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 1000; // 拆分阈值
        private final int start;
        private final int end;

        SumTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= THRESHOLD) {
                // 足够小，直接计算
                long sum = 0;
                for (int i = start; i <= end; i++) sum += i;
                return sum;
            }
            // 拆分：左半 + 右半
            int mid = (start + end) / 2;
            SumTask left = new SumTask(start, mid);
            SumTask right = new SumTask(mid + 1, end);
            left.fork();  // 异步执行左半
            long rightResult = right.compute(); // 当前线程执行右半
            long leftResult = left.join(); // 等待左半结果
            return leftResult + rightResult;
        }
    }

    // ==================== 4. CompletionService（提交与获取解耦）====================

    static void completionServiceDemo() throws Exception {
        System.out.println("===== CompletionService =====");

        // 场景：批量提交任务，按完成顺序获取结果（而非提交顺序）
        // ExecutorCompletionService 内部用 BlockingQueue 保存已完成的结果

        ExecutorService executor = Executors.newFixedThreadPool(3);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

        // 提交不同耗时的任务
        Object[][] delays = {{200, "慢"}, {50, "快"}, {100, "中"}};
        for (Object[] delay : delays) {
            final String name = delay[1] + "任务";
            final int ms = (int) delay[0];
            completionService.submit(() -> {
                Thread.sleep(ms);
                return name + "(耗时" + ms + "ms)";
            });
        }

        // 按完成顺序获取（快的先拿到）
        for (int i = 0; i < 3; i++) {
            Future<String> future = completionService.take(); // 阻塞等待下一个完成的
            System.out.println("第" + (i + 1) + "个完成: " + future.get());
        }

        executor.shutdown();
        System.out.println();
    }

    // ==================== 5. ScheduledExecutorService（定时调度）====================

    static void scheduledExecutorDemo() throws Exception {
        System.out.println("===== ScheduledExecutorService =====");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // 5.1 schedule: 延迟执行一次
        scheduler.schedule(() ->
            System.out.println("延迟200ms执行"), 200, TimeUnit.MILLISECONDS);

        // 5.2 scheduleAtFixedRate: 固定频率（不管上次执行多久）
        // 适合：需要稳定执行频率的任务
        ScheduledFuture<?> rateFuture = scheduler.scheduleAtFixedRate(() ->
            System.out.println("fixedRate: " + System.currentTimeMillis() % 10000),
            0, 100, TimeUnit.MILLISECONDS);

        // 5.3 scheduleWithFixedDelay: 固定延迟（上次结束后等固定时间再执行）
        // 适合：需要稳定间隔的任务（避免任务重叠）
        ScheduledFuture<?> delayFuture = scheduler.scheduleWithFixedDelay(() ->
            System.out.println("fixedDelay: " + System.currentTimeMillis() % 10000),
            0, 150, TimeUnit.MILLISECONDS);

        // 运行一段时间后取消
        Thread.sleep(500);
        rateFuture.cancel(false);
        delayFuture.cancel(false);
        scheduler.shutdown();
        scheduler.awaitTermination(1, TimeUnit.SECONDS);
        System.out.println();
    }

    // ==================== 6. LinkedTransferQueue ====================

    static void linkedTransferQueueDemo() throws Exception {
        System.out.println("===== LinkedTransferQueue =====");

        // 结合了 ConcurrentLinkedQueue 和 SynchronousQueue 的特点
        // - transfer(): 如果有消费者在等待，直接传递；否则入队阻塞直到被消费
        // - tryTransfer(): 非阻塞尝试
        // - tryTransfer(e, timeout): 超时尝试

        LinkedTransferQueue<String> queue = new LinkedTransferQueue<>();

        // 消费者先等待
        Thread consumer = new Thread(() -> {
            try {
                String msg = queue.take();
                System.out.println("消费者收到: " + msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();
        Thread.sleep(50);

        // transfer: 直接传递给等待的消费者（不经过队列）
        queue.transfer("direct-transfer");

        // tryTransfer: 无消费者时入队
        boolean transferred = queue.tryTransfer("queued-message");
        System.out.println("tryTransfer(无消费者): " + transferred + ", 队列大小=" + queue.size());

        consumer.join();
        System.out.println();
    }

    // ==================== 7. ConcurrentSkipListMap ====================

    static void concurrentSkipListDemo() throws Exception {
        System.out.println("===== ConcurrentSkipListMap =====");

        // 基于跳表（Skip List）实现的并发有序Map
        // 类似 TreeMap 的并发版本，但读操作无锁
        // 操作复杂度：O(log n)

        ConcurrentSkipListMap<Integer, String> map = new ConcurrentSkipListMap<>();

        // 并发写入
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int base = i * 10;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    map.put(base + j, "value-" + (base + j));
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        // 有序遍历
        System.out.println("有序key(前10): " + map.keySet().stream().limit(10).toList());

        // 范围查询
        System.out.println("subMap(5,15): " + map.subMap(5, true, 15, false).keySet());

        // 导航方法
        System.out.println("firstKey: " + map.firstKey());
        System.out.println("lastKey: " + map.lastKey());
        System.out.println("lowerKey(25): " + map.lowerKey(25));
        System.out.println("higherKey(25): " + map.higherKey(25));
        System.out.println("floorKey(25): " + map.floorKey(25));
        System.out.println("ceilingKey(25): " + map.ceilingKey(25));

        // ConcurrentSkipListSet 同理（有序并发Set）
        System.out.println();
    }
}
