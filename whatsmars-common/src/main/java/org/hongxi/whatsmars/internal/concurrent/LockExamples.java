package org.hongxi.whatsmars.internal.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * 锁机制示例：synchronized、ReentrantLock、ReadWriteLock、StampedLock
 */
class LockExamples {

    public static void main(String[] args) throws Exception {
        synchronizedDemo();
        reentrantLockDemo();
        readWriteLockDemo();
        stampedLockDemo();
    }

    // ==================== 1. synchronized 三种用法 ====================

    static void synchronizedDemo() throws Exception {
        System.out.println("===== synchronized =====");

        // 1.1 修饰实例方法 -> 锁当前对象(this)
        // 1.2 修饰静态方法 -> 锁当前Class对象
        // 1.3 修饰代码块   -> 锁指定对象

        Counter counter = new Counter();
        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("synchronized计数结果(期望10000): " + counter.count);

        // synchronized 可重入：同一线程可重复获取同一把锁
        Reentrant re = new Reentrant();
        re.outer(); // 不会死锁
        System.out.println();
    }

    static class Counter {
        int count = 0;
        synchronized void increment() { count++; }
    }

    static class Reentrant {
        synchronized void outer() {
            inner(); // 同一线程再次获取this锁，可重入
        }
        synchronized void inner() {
            // 成功进入，证明可重入
        }
    }

    // ==================== 2. ReentrantLock ====================

    static void reentrantLockDemo() throws Exception {
        System.out.println("===== ReentrantLock =====");

        // 相比 synchronized 的额外能力：
        // - tryLock(): 非阻塞尝试获取锁
        // - lockInterruptibly(): 可响应中断的锁获取
        // - newCondition(): 精确唤醒（替代 wait/notify）
        // - fair lock: 公平锁模式

        ReentrantLock lock = new ReentrantLock();
        Condition notFull  = lock.newCondition();
        Condition notEmpty = lock.newCondition();

        int[] buffer = new int[3];
        int[] count = {0};

        Thread producer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                lock.lock();
                try {
                    while (count[0] == buffer.length) {
                        System.out.println("生产者等待(队列满)...");
                        notFull.await();
                    }
                    buffer[count[0]++] = i;
                    System.out.println("生产: " + i + ", 当前数量: " + count[0]);
                    notEmpty.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    lock.unlock();
                }
            }
        });

        Thread consumer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                lock.lock();
                try {
                    while (count[0] == 0) {
                        notEmpty.await();
                    }
                    int val = buffer[--count[0]];
                    System.out.println("消费: " + val + ", 当前数量: " + count[0]);
                    notFull.signal();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    lock.unlock();
                }
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        // tryLock 示例
        ReentrantLock rl = new ReentrantLock();
        if (rl.tryLock()) {
            try {
                System.out.println("tryLock 成功获取锁");
            } finally {
                rl.unlock();
            }
        }
        System.out.println();
    }

    // ==================== 3. ReadWriteLock 读写锁 ====================

    static void readWriteLockDemo() throws Exception {
        System.out.println("===== ReadWriteLock =====");

        // 读读不互斥，读写互斥，写写互斥
        // 适合读多写少场景（如缓存）

        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
        String[] data = {"initial"};

        // 读操作：共享锁，多个读线程可并发
        Thread[] readers = new Thread[3];
        for (int i = 0; i < readers.length; i++) {
            final int id = i;
            readers[i] = new Thread(() -> {
                rwLock.readLock().lock();
                try {
                    System.out.println("Reader-" + id + " 读取: " + data[0]);
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                } finally {
                    rwLock.readLock().unlock();
                }
            });
            readers[i].start();
        }
        for (Thread r : readers) r.join();

        // 写操作：独占锁
        Thread writer = new Thread(() -> {
            rwLock.writeLock().lock();
            try {
                data[0] = "updated-" + System.currentTimeMillis();
                System.out.println("Writer 更新: " + data[0]);
            } finally {
                rwLock.writeLock().unlock();
            }
        });
        writer.start();
        writer.join();
        System.out.println();
    }

    // ==================== 4. StampedLock（JDK 8+）====================

    static void stampedLockDemo() throws Exception {
        System.out.println("===== StampedLock =====");

        // 三种模式：
        // 1. Writing（写锁，独占）
        // 2. Reading（读锁，共享，类似ReadWriteLock）
        // 3. Optimistic Reading（乐观读，无锁，性能最高）

        StampedLock sl = new StampedLock();
        double x = 1.0, y = 1.0;

        // 写操作
        long stamp = sl.writeLock();
        try {
            x = 2.0;
            y = 3.0;
        } finally {
            sl.unlockWrite(stamp);
        }

        // 乐观读：不加锁，先读数据，再校验是否有写操作介入
        stamp = sl.tryOptimisticRead();
        double curX = x, curY = y;
        if (!sl.validate(stamp)) {
            // 校验失败，说明期间有写操作，升级为悲观读锁重新读
            stamp = sl.readLock();
            try {
                curX = x;
                curY = y;
            } finally {
                sl.unlockRead(stamp);
            }
        }
        System.out.println("乐观读结果: x=" + curX + ", y=" + curY);

        // 锁转换：写锁降级为读锁
        stamp = sl.writeLock();
        try {
            x = 4.0;
            long readStamp = sl.tryConvertToReadLock(stamp); // 写锁降级
            try {
                System.out.println("写锁降级为读锁, x=" + x);
            } finally {
                sl.unlockRead(readStamp);
            }
        } finally {
            // 如果降级成功则已释放，否则释放写锁
            // 实际使用中需判断 readStamp != stamp
        }
        System.out.println();
    }
}
