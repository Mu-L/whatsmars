package org.hongxi.whatsmars.internal.concurrent;

/**
 * volatile 示例：可见性、有序性、DCL单例模式
 */
class VolatileExamples {

    public static void main(String[] args) throws Exception {
        visibilityDemo();
        orderDemo();
        doubleCheckedLocking();
    }

    // ==================== 1. 可见性问题 ====================

    static void visibilityDemo() throws Exception {
        System.out.println("===== volatile 可见性 =====");

        // 没有 volatile：线程A修改变量，线程B可能永远看不到（CPU缓存）
        // 有 volatile：写操作立即刷到主内存，读操作从主内存读取

        // 错误示例（可能死循环）：
        // boolean running = true;
        // 线程A: while(running) { ... }
        // 线程B: running = false;  // 线程A可能看不到这个修改

        // 正确做法：
        VolatileFlag flag = new VolatileFlag();
        Thread worker = new Thread(() -> {
            while (flag.running) {
                // 工作中
            }
            System.out.println("worker 检测到 running=false，退出");
        });
        worker.start();

        Thread.sleep(100);
        flag.running = false; // volatile 写，对其他线程立即可见
        worker.join();
        System.out.println();
    }

    static class VolatileFlag {
        volatile boolean running = true;
    }

    // ==================== 2. 有序性问题（指令重排）====================

    static void orderDemo() throws Exception {
        System.out.println("===== volatile 有序性 =====");

        // 经典问题：双重检查锁定(DCL)中为什么需要 volatile？
        // new Singleton() 不是原子操作，分为三步：
        // 1. 分配内存
        // 2. 初始化对象
        // 3. 引用指向内存
        //
        // 没有 volatile 时，2和3可能被重排为 1->3->2
        // 此时另一个线程可能拿到未初始化的对象！

        // 演示重排序（不保证每次复现，但理论上存在）
        int[] result = new int[2];
        boolean[] ready = new boolean[2];

        Thread t1 = new Thread(() -> {
            // 线程A：写操作
            result[0] = 1;    // (a)
            ready[0] = true;  // (b) 可能被重排到(a)之前
        });

        Thread t2 = new Thread(() -> {
            // 线程B：读操作
            if (ready[0]) {    // 如果看到(b)为true
                result[1] = result[0]; // 但(a)可能还没执行
            }
        });

        // 多次运行以增加复现概率
        int reordered = 0;
        for (int i = 0; i < 10000; i++) {
            result[0] = 0;
            result[1] = 0;
            ready[0] = false;
            t1 = new Thread(() -> {
                result[0] = 1;
                ready[0] = true;
            });
            t2 = new Thread(() -> {
                if (ready[0]) {
                    result[1] = result[0];
                }
            });
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            if (ready[0] && result[1] == 0) {
                reordered++;
            }
        }
        System.out.println("观察到重排序的次数(可能为0): " + reordered);
        System.out.println("注意：JIT优化程度取决于JVM实现，此处仅为演示");
        System.out.println();
    }

    // ==================== 3. DCL 双重检查锁定单例 ====================

    static void doubleCheckedLocking() throws Exception {
        System.out.println("===== DCL 单例模式 =====");

        // 验证单例
        Singleton s1 = Singleton.getInstance();
        Singleton s2 = Singleton.getInstance();
        System.out.println("s1 == s2: " + (s1 == s2));

        // 多线程验证
        java.util.Set<Singleton> set = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        Thread[] threads = new Thread[20];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> set.add(Singleton.getInstance()));
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("20个线程获取的实例数(应为1): " + set.size());
        System.out.println();
    }

    static class Singleton {
        // volatile 防止指令重排导致拿到半初始化对象
        private static volatile Singleton instance;

        private Singleton() {}

        static Singleton getInstance() {
            if (instance == null) {                // 第一次检查（无锁，快速路径）
                synchronized (Singleton.class) {   // 加锁
                    if (instance == null) {        // 第二次检查（防止重复创建）
                        instance = new Singleton();
                    }
                }
            }
            return instance;
        }
    }
}
