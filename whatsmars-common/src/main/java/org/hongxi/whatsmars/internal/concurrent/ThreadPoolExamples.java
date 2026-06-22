package org.hongxi.whatsmars.internal.concurrent;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池用法示例：参数配置、拒绝策略、优雅关闭、Future批量提交
 */
class ThreadPoolExamples {

    public static void main(String[] args) throws Exception {
        coreParameters();
        rejectionPolicy();
        gracefulShutdown();
        futureBatch();
    }

    // ==================== 1. ThreadPoolExecutor 七大核心参数 ====================

    static void coreParameters() throws Exception {
        System.out.println("===== 线程池核心参数 =====");

        // 七大参数：
        // 1. corePoolSize    - 核心线程数（即使空闲也不回收，除非设置 allowCoreThreadTimeOut）
        // 2. maximumPoolSize - 最大线程数
        // 3. keepAliveTime   - 非核心线程空闲存活时间
        // 4. unit            - 时间单位
        // 5. workQueue       - 任务队列（LinkedBlockingQueue / ArrayBlockingQueue / SynchronousQueue 等）
        // 6. threadFactory   - 线程工厂（可自定义线程名称方便排查）
        // 7. handler         - 拒绝策略（AbortPolicy / CallerRunsPolicy / DiscardPolicy / DiscardOldestPolicy）

        AtomicInteger counter = new AtomicInteger(0);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            2,                              // corePoolSize
            4,                              // maximumPoolSize
            60, TimeUnit.SECONDS,           // keepAliveTime + unit
            new ArrayBlockingQueue<>(2),    // workQueue 容量为2
            r -> {
                Thread t = new Thread(r, "biz-thread-" + counter.incrementAndGet());
                t.setDaemon(false);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝时由调用线程执行
        );

        // 执行流程：
        // 1. 任务数 <= corePoolSize(2)  -> 创建核心线程执行
        // 2. 任务数 > 2 且队列未满(2)   -> 放入队列
        // 3. 队列满 且 任务数 < max(4)  -> 创建非核心线程执行
        // 4. 任务数 > max(4)            -> 触发拒绝策略
        for (int i = 0; i < 6; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    System.out.println("task-" + taskId + " running in " + Thread.currentThread().getName());
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                });
                System.out.printf("提交task-%d | 活跃线程=%d, 队列大小=%d, 已完成=%d%n",
                    taskId, executor.getActiveCount(),
                    executor.getQueue().size(), executor.getCompletedTaskCount());
            } catch (RejectedExecutionException e) {
                System.out.println("task-" + taskId + " 被拒绝!");
            }
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println();
    }

    // ==================== 2. 四种拒绝策略 ====================

    static void rejectionPolicy() {
        System.out.println("===== 拒绝策略 =====");
        // AbortPolicy        - 抛出 RejectedExecutionException（默认）
        // CallerRunsPolicy   - 由提交任务的线程自己执行（降级，不丢任务）
        // DiscardPolicy      - 静默丢弃（不抛异常）
        // DiscardOldestPolicy - 丢弃队列中最老的任务，重新提交当前任务
        System.out.println("AbortPolicy:        抛异常，调用方可catch降级处理");
        System.out.println("CallerRunsPolicy:   调用方线程执行，天然限流");
        System.out.println("DiscardPolicy:      静默丢弃，适合可丢失任务");
        System.out.println("DiscardOldestPolicy:丢弃最旧任务，适合时效性任务");
        System.out.println();
    }

    // ==================== 3. 优雅关闭 ====================

    static void gracefulShutdown() throws Exception {
        System.out.println("===== 优雅关闭 =====");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 3; i++) {
            final int id = i;
            executor.execute(() -> {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                System.out.println("task-" + id + " done");
            });
        }

        // shutdown(): 不再接受新任务，等待已提交任务完成
        executor.shutdown();

        // shutdownNow(): 尝试中断所有执行中的线程，返回未执行的任务列表
        // List<Runnable> pending = executor.shutdownNow();

        if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
            System.out.println("超时未结束，强制关闭");
            executor.shutdownNow();
        }
        System.out.println("线程池已关闭: isTerminated=" + executor.isTerminated());
        System.out.println();
    }

    // ==================== 4. Future 批量提交 ====================

    static void futureBatch() throws Exception {
        System.out.println("===== Future批量提交 =====");

        ExecutorService executor = Executors.newFixedThreadPool(3);

        // invokeAll: 批量提交，等待所有完成
        List<Callable<String>> tasks = List.of(
            () -> { Thread.sleep(100); return "task-A"; },
            () -> { Thread.sleep(50);  return "task-B"; },
            () -> { Thread.sleep(150); return "task-C"; }
        );

        long start = System.currentTimeMillis();
        List<Future<String>> futures = executor.invokeAll(tasks);
        for (Future<String> f : futures) {
            System.out.println("结果: " + f.get());
        }
        System.out.println("invokeAll总耗时: " + (System.currentTimeMillis() - start) + "ms（并行执行）");

        // invokeAny: 批量提交，任一成功即返回（适合多路冗余调用）
        String fastest = executor.invokeAny(tasks);
        System.out.println("invokeAny最快结果: " + fastest);

        executor.shutdown();
        System.out.println();
    }
}
