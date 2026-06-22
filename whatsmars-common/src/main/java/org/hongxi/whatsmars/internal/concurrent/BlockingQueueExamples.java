package org.hongxi.whatsmars.internal.concurrent;

import java.util.concurrent.*;

/**
 * 阻塞队列示例：ArrayBlockingQueue、LinkedBlockingQueue、SynchronousQueue、DelayQueue
 */
class BlockingQueueExamples {

    public static void main(String[] args) throws Exception {
        arrayBlockingQueueDemo();
        linkedBlockingQueueDemo();
        synchronousQueueDemo();
        delayQueueDemo();
    }

    // ==================== 1. ArrayBlockingQueue（有界）====================

    static void arrayBlockingQueueDemo() throws Exception {
        System.out.println("===== ArrayBlockingQueue =====");

        // 有界队列，必须在构造时指定容量
        // 公平模式：按FIFO顺序获取元素（默认非公平，吞吐量更高）
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(3);

        // 四种操作方式：
        // 1. 抛异常：add()/remove()/element()
        // 2. 返回特殊值：offer()/poll()/peek()
        // 3. 阻塞：put()/take()
        // 4. 超时：offer(e, timeout, unit) / poll(timeout, unit)

        // 生产者-消费者模式
        ArrayBlockingQueue<Integer> abq = new ArrayBlockingQueue<>(2);
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    abq.put(i); // 队列满时阻塞
                    System.out.println("生产: " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        Thread consumer = new Thread(() -> {
            try {
                Thread.sleep(300); // 延迟消费，让队列满
                for (int i = 0; i < 5; i++) {
                    int val = abq.take(); // 队列空时阻塞
                    System.out.println("消费: " + val);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        System.out.println();
    }

    // ==================== 2. LinkedBlockingQueue（可选有界）====================

    static void linkedBlockingQueueDemo() throws Exception {
        System.out.println("===== LinkedBlockingQueue =====");

        // 不指定容量时默认为 Integer.MAX_VALUE（近似无界）
        // 生产者和消费者各用一把独立的锁，并发度更高

        // 推荐：始终指定容量，避免OOM
        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>(100);

        lbq.put("hello");
        lbq.put("world");

        // offer 带超时
        boolean offered = lbq.offer("timeout-test", 1, TimeUnit.SECONDS);
        System.out.println("offer带超时: " + offered);

        // drainTo: 批量取出（减少锁竞争）
        java.util.List<String> batch = new java.util.ArrayList<>();
        lbq.drainTo(batch, 10); // 最多取10个
        System.out.println("drainTo取出: " + batch);
        System.out.println();
    }

    // ==================== 3. SynchronousQueue（零容量）====================

    static void synchronousQueueDemo() throws Exception {
        System.out.println("===== SynchronousQueue =====");

        // 没有容量，每个put必须等待take，反之亦然
        // 适合：线程间一对一直接传递（如线程池Executors.newCachedThreadPool）

        SynchronousQueue<Integer> sq = new SynchronousQueue<>();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    sq.put(i); // 阻塞直到有线程take
                    System.out.println("传递: " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    Thread.sleep(100);
                    int val = sq.take(); // 阻塞直到有线程put
                    System.out.println("接收: " + val);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        System.out.println();
    }

    // ==================== 4. DelayQueue（延迟队列）====================

    static void delayQueueDemo() throws Exception {
        System.out.println("===== DelayQueue =====");

        // 元素必须实现 Delayed 接口
        // 只有延迟时间到期后才能 take()
        // 适合：定时任务、缓存过期、订单超时取消

        DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();
        delayQueue.put(new DelayedTask("task-3s", 3000));
        delayQueue.put(new DelayedTask("task-1s", 1000));
        delayQueue.put(new DelayedTask("task-2s", 2000));

        long start = System.currentTimeMillis();
        while (!delayQueue.isEmpty()) {
            DelayedTask task = delayQueue.take(); // 阻塞直到延迟到期
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("取出: " + task.name + ", 耗时: " + elapsed + "ms");
        }
        System.out.println();
    }

    static class DelayedTask implements Delayed {
        final String name;
        final long triggerTime; // 绝对时间(ms)

        DelayedTask(String name, long delayMs) {
            this.name = name;
            this.triggerTime = System.currentTimeMillis() + delayMs;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(triggerTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(this.triggerTime, ((DelayedTask) o).triggerTime);
        }
    }
}
