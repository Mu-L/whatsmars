package org.hongxi.whatsmars.internal.concurrent;

import java.util.concurrent.*;

/**
 * CompletableFuture 异步编程示例：链式调用、组合、异常处理、超时
 */
class CompletableFutureExamples {

    public static void main(String[] args) throws Exception {
        basicAsync();
        chaining();
        combining();
        exceptionHandling();
        timeout();
    }

    // ==================== 1. 基础异步操作 ====================

    static void basicAsync() throws Exception {
        System.out.println("===== CompletableFuture 基础 =====");

        // 1.1 supplyAsync: 有返回值的异步任务
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            return "hello";
        });

        // 1.2 runAsync: 无返回值的异步任务
        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() ->
            System.out.println("runAsync 任务执行")
        );

        // 1.3 指定线程池
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CompletableFuture<String> cf3 = CompletableFuture.supplyAsync(() -> "from-pool", executor);

        System.out.println("supplyAsync结果: " + cf1.get());
        cf2.get();
        System.out.println("指定线程池结果: " + cf3.get());
        executor.shutdown();
        System.out.println();
    }

    // ==================== 2. 链式调用 ====================

    static void chaining() throws Exception {
        System.out.println("===== 链式调用 =====");

        // thenApply: 同步转换（类似 map）
        // thenApplyAsync: 异步转换（提交到新任务）
        // thenAccept: 消费结果，无返回值
        // thenRun: 不关心上游结果，只执行动作

        String result = CompletableFuture.supplyAsync(() -> "hello")
            .thenApply(s -> s + " world")        // 同步转换
            .thenApply(String::toUpperCase)       // 继续转换
            .thenApply(s -> {
                System.out.println("  中间值: " + s);
                return s;
            })
            .get();
        System.out.println("链式结果: " + result);

        // thenCompose: 扁平化（类似 flatMap），避免嵌套 CompletableFuture
        CompletableFuture<String> composed = CompletableFuture.supplyAsync(() -> "hello")
            .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " composed"));
        System.out.println("thenCompose结果: " + composed.get());
        System.out.println();
    }

    // ==================== 3. 组合多个 Future ====================

    static void combining() throws Exception {
        System.out.println("===== 组合 =====");

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            return "result-1";
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            return "result-2";
        });

        // allOf: 等待所有完成
        CompletableFuture.allOf(future1, future2).join();
        System.out.println("allOf: " + future1.get() + ", " + future2.get());

        // anyOf: 任一完成即返回
        Object fastest = CompletableFuture.anyOf(future1, future2).get();
        System.out.println("anyOf最快: " + fastest);

        // thenCombine: 两个Future都完成后合并结果
        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> 10);
        CompletableFuture<Integer> future4 = CompletableFuture.supplyAsync(() -> 20);
        CompletableFuture<Integer> combined = future3
            .thenCombine(future4, Integer::sum);
        System.out.println("thenCombine求和: " + combined.get());

        System.out.println();
    }

    // ==================== 4. 异常处理 ====================

    static void exceptionHandling() throws Exception {
        System.out.println("===== 异常处理 =====");

        // exceptionally: 异常时提供降级值（类似 catch）
        String result1 = CompletableFuture.<String>supplyAsync(() -> {
            throw new RuntimeException("模拟异常");
        }).exceptionally(ex -> {
            System.out.println("  异常: " + ex.getMessage());
            return "fallback-value";
        }).get();
        System.out.println("exceptionally结果: " + result1);

        // handle: 无论成功失败都处理（类似 finally + 转换）
        String result2 = CompletableFuture.<String>supplyAsync(() -> {
            throw new RuntimeException("error");
        }).handle((value, ex) -> {
            if (ex != null) {
                return "handled: " + ex.getMessage();
            }
            return value;
        }).get();
        System.out.println("handle结果: " + result2);

        // whenComplete: 完成时回调，不改变结果（类似 finally）
        CompletableFuture.supplyAsync(() -> "done")
            .whenComplete((value, ex) -> {
                if (ex == null) {
                    System.out.println("whenComplete: " + value);
                }
            })
            .get();
        System.out.println();
    }

    // ==================== 5. 超时控制（JDK 9+）====================

    static void timeout() throws Exception {
        System.out.println("===== 超时控制 =====");

        // orTimeout: 超时后抛出 TimeoutException
        try {
            CompletableFuture.supplyAsync(() -> {
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                return "too-late";
            }).orTimeout(100, TimeUnit.MILLISECONDS).get();
        } catch (ExecutionException e) {
            System.out.println("orTimeout触发: " + e.getCause().getClass().getSimpleName());
        }

        // completeOnTimeout: 超时后提供默认值
        String fallback = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            return "too-late";
        }).completeOnTimeout("default-value", 100, TimeUnit.MILLISECONDS).get();
        System.out.println("completeOnTimeout结果: " + fallback);

        System.out.println();
    }
}
