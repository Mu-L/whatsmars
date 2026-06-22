package org.hongxi.whatsmars.internal.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThreadLocal 示例：线程隔离、内存泄漏防范、InheritableThreadLocal
 */
class ThreadLocalExamples {

    public static void main(String[] args) throws Exception {
        basicUsage();
        dateFormatDemo();
        memoryLeakPrevention();
    }

    // ==================== 1. 基本用法：线程隔离 ====================

    static void basicUsage() throws Exception {
        System.out.println("===== ThreadLocal 基本用法 =====");

        // 每个线程拥有独立的变量副本，互不影响
        ThreadLocal<String> userContext = ThreadLocal.withInitial(() -> "anonymous");

        Thread[] threads = new Thread[3];
        for (int i = 0; i < threads.length; i++) {
            final String userId = "user-" + i;
            threads[i] = new Thread(() -> {
                userContext.set(userId);
                System.out.println(Thread.currentThread().getName() + " -> " + userContext.get());
                // 模拟业务处理
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                System.out.println(Thread.currentThread().getName() + " -> " + userContext.get());
                userContext.remove(); // 使用完毕后清理
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        // 主线程的值不受影响
        System.out.println("主线程: " + userContext.get());
        userContext.remove();
        System.out.println();
    }

    // ==================== 2. 经典场景：SimpleDateFormat 线程安全 ====================

    static void dateFormatDemo() throws Exception {
        System.out.println("===== SimpleDateFormat 线程安全 =====");

        // SimpleDateFormat 是非线程安全的！
        // 错误做法：共享一个实例
        // 正确做法：用 ThreadLocal 为每个线程创建独立实例

        ThreadLocal<java.text.SimpleDateFormat> dateFormat = ThreadLocal.withInitial(
            () -> new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        );

        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            final long time = System.currentTimeMillis() + i * 1000;
            executor.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName() + ": "
                        + dateFormat.get().format(new java.util.Date(time)));
                } finally {
                    // 线程池场景必须 remove，否则线程复用时数据错乱
                    dateFormat.remove();
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(2, java.util.concurrent.TimeUnit.SECONDS);
        System.out.println();
    }

    // ==================== 3. 内存泄漏防范 ====================

    static void memoryLeakPrevention() throws Exception {
        System.out.println("===== 内存泄漏防范 =====");

        // ThreadLocal 内存泄漏原理：
        // Thread -> ThreadLocalMap -> Entry(ThreadLocal, value)
        // Entry 的 key 是弱引用，但 value 是强引用
        // 如果线程长期存活（如线程池），value 不会被回收

        // 解决方案：
        // 1. 始终在 finally 中调用 remove()
        // 2. 使用 try-with-resources 模式封装

        // 封装自动清理的 ThreadLocal（JDK 无此内置，手动实现）
        AutoCleanThreadLocal<String> autoClean = new AutoCleanThreadLocal<>();

        Thread t = new Thread(() -> {
            try (AutoCleanThreadLocal<String>.Scope scope = autoClean.set("temp-value")) {
                System.out.println("自动清理模式: " + autoClean.get());
            } // scope.close() 自动 remove
            System.out.println("清理后: " + autoClean.get());
        });
        t.start();
        t.join();

        // 线程池场景最佳实践：
        // - 使用 Filter/Interceptor 在请求开始时 set，结束时 remove
        // - Spring 的 RequestContextHolder 就是这个模式
        System.out.println();
    }

    /**
     * 封装可自动清理的 ThreadLocal
     */
    static class AutoCleanThreadLocal<T> {
        private final ThreadLocal<T> tl = new ThreadLocal<>();

        T get() {
            return tl.get();
        }

        Scope set(T value) {
            tl.set(value);
            return new Scope();
        }

        class Scope implements AutoCloseable {
            @Override
            public void close() {
                tl.remove();
            }
        }
    }
}
