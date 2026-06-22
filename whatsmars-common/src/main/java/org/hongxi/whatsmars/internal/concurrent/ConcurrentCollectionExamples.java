package org.hongxi.whatsmars.internal.concurrent;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 并发容器示例：ConcurrentHashMap、CopyOnWriteArrayList、BlockingQueue(见BlockingQueueExamples)、ConcurrentLinkedQueue
 */
class ConcurrentCollectionExamples {

    public static void main(String[] args) throws Exception {
        concurrentHashMapDemo();
        copyOnWriteArrayListDemo();
        concurrentLinkedQueueDemo();
        priorityBlockingQueueDemo();
    }

    // ==================== 1. ConcurrentHashMap ====================

    static void concurrentHashMapDemo() throws Exception {
        System.out.println("===== ConcurrentHashMap =====");

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        // 线程安全的复合操作（JDK 8+）
        map.put("a", 1);
        map.computeIfAbsent("b", k -> 2);           // 原子性 put if absent
        map.computeIfPresent("a", (k, v) -> v + 1); // 原子性 update
        map.merge("c", 1, Integer::sum);             // 原子性 merge
        System.out.println("compute/merge操作后: " + map);

        // 注意：size() 是弱一致性，非精确值
        // 不要用 size() 做精确判断后再操作，应使用 mappingCount()（返回long，更准确）
        System.out.println("mappingCount: " + map.mappingCount());

        // 并行遍历（利用多核）
        map.put("d", 4);
        map.put("e", 5);
        map.forEach(2, (k, v) ->
            System.out.println("并行遍历(parallelismThreshold=2): " + k + "=" + v)
        );

        // 并发写入测试
        ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    // 错误示范：counter.put("key", counter.getOrDefault("key", 0) + 1) 非原子！
                    // 正确做法：
                    counter.merge("counter-" + idx, 1, Integer::sum);
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("CHM并发计数(每个key期望1000): " + counter);
        System.out.println();
    }

    // ==================== 2. CopyOnWriteArrayList ====================

    static void copyOnWriteArrayListDemo() throws Exception {
        System.out.println("===== CopyOnWriteArrayList =====");

        // 写时复制：每次写操作都复制整个数组
        // 读操作无锁（直接读底层数组），写操作加锁
        // 适合：读多写少，如监听器列表、配置列表

        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        // 迭代器是快照，不会抛 ConcurrentModificationException
        Iterator<String> it = list.iterator();
        list.add("d"); // 修改原列表不影响迭代器
        System.out.print("迭代器快照遍历: ");
        while (it.hasNext()) {
            System.out.print(it.next() + " ");
        }
        System.out.println();
        System.out.println("实际列表: " + list);

        // 注意：写操作开销大（复制整个数组），不适合写频繁的场景
        // 如果写多读少，用 Collections.synchronizedList 或 ReentrantLock
        System.out.println();
    }

    // ==================== 3. ConcurrentLinkedQueue ====================

    static void concurrentLinkedQueueDemo() throws Exception {
        System.out.println("===== ConcurrentLinkedQueue =====");

        // 无界非阻塞队列，基于CAS实现
        // 适合：生产者消费者场景，不需要阻塞等待
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();

        Thread[] producers = new Thread[5];
        for (int i = 0; i < producers.length; i++) {
            final int id = i;
            producers[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    queue.offer(id * 100 + j);
                }
            });
            producers[i].start();
        }
        for (Thread t : producers) t.join();
        System.out.println("并发入队后size: " + queue.size());

        // poll() 非阻塞，队列为空时返回null
        int count = 0;
        while (queue.poll() != null) {
            count++;
        }
        System.out.println("出队数量: " + count);
        System.out.println();
    }

    // ==================== 4. PriorityBlockingQueue ====================

    static void priorityBlockingQueueDemo() throws Exception {
        System.out.println("===== PriorityBlockingQueue =====");

        // 无界阻塞优先级队列，元素按自然顺序或Comparator排序
        // take() 在队列为空时阻塞
        PriorityBlockingQueue<Task> pq = new PriorityBlockingQueue<>(10,
            Comparator.comparingInt(t -> t.priority));

        pq.offer(new Task("low", 3));
        pq.offer(new Task("high", 1));
        pq.offer(new Task("medium", 2));

        // 按优先级出队
        System.out.print("按优先级出队: ");
        while (!pq.isEmpty()) {
            System.out.print(pq.poll() + " ");
        }
        System.out.println();
        System.out.println();
    }

    static class Task {
        final String name;
        final int priority;

        Task(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public String toString() {
            return name + "(" + priority + ")";
        }
    }
}
