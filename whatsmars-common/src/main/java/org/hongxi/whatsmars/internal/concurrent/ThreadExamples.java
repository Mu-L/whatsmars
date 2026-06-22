package org.hongxi.whatsmars.internal.concurrent;

/**
 * 线程基础用法示例：创建方式、生命周期状态、中断机制、守护线程
 */
class ThreadExamples {

    public static void main(String[] args) throws Exception {
        threadCreation();
        threadState();
        threadInterrupt();
        daemonThread();
    }

    // ==================== 1. 线程创建的三种方式 ====================

    static void threadCreation() throws Exception {
        System.out.println("===== 线程创建方式 =====");

        // 方式一：继承 Thread
        Thread t1 = new Thread() {
            @Override
            public void run() {
                System.out.println("[继承Thread] " + Thread.currentThread().getName());
            }
        };
        t1.start();
        t1.join();

        // 方式二：实现 Runnable（推荐，避免单继承限制）
        Thread t2 = new Thread(() ->
            System.out.println("[实现Runnable] " + Thread.currentThread().getName())
        );
        t2.start();
        t2.join();

        // 方式三：实现 Callable + FutureTask（可获取返回值）
        java.util.concurrent.FutureTask<String> task = new java.util.concurrent.FutureTask<>(() -> {
            System.out.println("[Callable] " + Thread.currentThread().getName());
            return "result-from-callable";
        });
        Thread t3 = new Thread(task);
        t3.start();
        System.out.println("[Callable返回值] " + task.get());
        System.out.println();
    }

    // ==================== 2. 线程六种状态 ====================

    static void threadState() throws Exception {
        System.out.println("===== 线程状态 =====");

        Thread t = new Thread(() -> {
            // NEW -> RUNNABLE
            try {
                // RUNNABLE -> TIMED_WAITING
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // RUNNABLE (被中断后恢复)
            }
            // RUNNABLE -> BLOCKED (等待获取synchronized锁)
            synchronized (ThreadExamples.class) {
                // do nothing
            }
            // RUNNABLE -> TERMINATED
        });

        System.out.println("NEW:          " + t.getState());
        t.start();
        System.out.println("RUNNABLE:     " + t.getState());

        Thread.sleep(50);
        System.out.println("TIMED_WAITING:" + t.getState());

        t.join();
        System.out.println("TERMINATED:   " + t.getState());
        System.out.println();
    }

    // ==================== 3. 中断机制 ====================

    static void threadInterrupt() throws Exception {
        System.out.println("===== 中断机制 =====");

        // 3.1 通过检查中断标志优雅退出
        Thread t1 = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // 工作中...
            }
            System.out.println("t1: 检测到中断标志，退出");
        });
        t1.start();
        t1.interrupt();
        t1.join();

        // 3.2 sleep/wait/join 中收到中断会抛出 InterruptedException 并清除中断标志
        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                // 中断标志已被清除，需要重新设置（如果需要向上传播）
                Thread.currentThread().interrupt();
                System.out.println("t2: sleep被中断, isInterrupted=" + Thread.currentThread().isInterrupted());
            }
        });
        t2.start();
        Thread.sleep(50);
        t2.interrupt();
        t2.join();

        // 3.3 interrupted() 是静态方法，会清除中断标志；isInterrupted() 是实例方法，不会清除
        Thread.currentThread().interrupt();
        System.out.println("interrupted()=true (第一次): " + Thread.interrupted());
        System.out.println("interrupted()=false (已清除): " + Thread.interrupted());
        System.out.println();
    }

    // ==================== 4. 守护线程 ====================

    static void daemonThread() throws Exception {
        System.out.println("===== 守护线程 =====");

        Thread daemon = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(100);
                    System.out.println("守护线程运行中... isDaemon=" + Thread.currentThread().isDaemon());
                }
            } catch (InterruptedException e) {
                System.out.println("守护线程被中断，JVM即将退出");
            }
        });
        daemon.setDaemon(true); // 必须在 start() 之前设置
        daemon.start();

        // 主线程（非守护线程）结束后，JVM 不会等待守护线程
        Thread.sleep(350);
        System.out.println("主线程结束 -> JVM退出 -> 守护线程自动终止");
        System.out.println();
    }
}
