package org.hongxi.whatsmars.curator.examples;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.hongxi.whatsmars.curator.EmbeddedZookeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Curator Distributed Lock Examples
 * 
 * Apache Curator 提供了多种分布式锁实现：
 * 1. InterProcessMutex - 可重入的互斥锁
 * 2. InterProcessSemaphoreMutex - 不可重入的互斥锁
 * 3. InterProcessReadWriteLock - 读写锁
 * 4. InterProcessMultiLock - 多锁组合
 */
public class DistributedLockExamples {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedLockExamples.class);
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final String LOCK_PATH = "/curator/locks";
    
    /**
     * 示例 1: InterProcessMutex - 可重入互斥锁
     * 
     * InterProcessMutex 是最常用的分布式锁实现，特点：
     * 1. 可重入：同一个线程可以多次获取锁
     * 2. 基于 ZK 的临时顺序节点实现
     * 3. 支持公平锁（按顺序获取）
     */
    public static class DistributedLock implements Closeable {
        
        private final CuratorFramework client;
        private final InterProcessMutex lock;
        private final String lockName;
        
        public DistributedLock(String lockName) {
            this.lockName = lockName;
            
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            lock = new InterProcessMutex(client, LOCK_PATH + "/" + lockName);
        }
        
        /**
         * 获取锁（阻塞等待）
         */
        public void acquire() throws Exception {
            lock.acquire();
            logger.info("线程 {} 获取锁成功: {}", Thread.currentThread().getName(), lockName);
        }
        
        /**
         * 获取锁（带超时）
         * @param timeout 超时时间
         * @param unit 时间单位
         * @return 是否获取成功
         */
        public boolean acquire(long timeout, TimeUnit unit) throws Exception {
            boolean acquired = lock.acquire(timeout, unit);
            if (acquired) {
                logger.info("线程 {} 在 {} {} 内获取锁成功: {}", 
                    Thread.currentThread().getName(), timeout, unit, lockName);
            } else {
                logger.warn("线程 {} 在 {} {} 内未能获取锁: {}", 
                    Thread.currentThread().getName(), timeout, unit, lockName);
            }
            return acquired;
        }
        
        /**
         * 释放锁
         */
        public void release() throws Exception {
            lock.release();
            logger.info("线程 {} 释放锁: {}", Thread.currentThread().getName(), lockName);
        }
        
        /**
         * 检查当前线程是否持有锁
         */
        public boolean isOwnedByCurrentThread() {
            return lock.isOwnedByCurrentThread();
        }
        
        @Override
        public void close() throws IOException {
            try {
                if (lock.isOwnedByCurrentThread()) {
                    lock.release();
                }
            } catch (Exception e) {
                logger.error("释放锁失败", e);
            }
            client.close();
        }
    }
    
    /**
     * 示例 2: InterProcessReadWriteLock - 读写锁
     * 
     * 读写锁允许：
     * 1. 多个读锁同时持有（读操作并发）
     * 2. 写锁独占（写操作互斥）
     * 3. 读写互斥
     */
    public static class DistributedReadWriteLock implements Closeable {
        
        private final CuratorFramework client;
        private final InterProcessReadWriteLock lock;
        private final String lockName;
        
        public DistributedReadWriteLock(String lockName) {
            this.lockName = lockName;
            
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            lock = new InterProcessReadWriteLock(client, LOCK_PATH + "/" + lockName);
        }
        
        public InterProcessLock readLock() {
            return lock.readLock();
        }
        
        public InterProcessLock writeLock() {
            return lock.writeLock();
        }
        
        @Override
        public void close() throws IOException {
            client.close();
        }
    }
    
    /**
     * 示例 3: 使用分布式锁保护共享资源
     */
    public static class SharedResource implements AutoCloseable {
        
        private final DistributedLock lock;
        private int counter = 0;
        
        public SharedResource(String resourceName) {
            this.lock = new DistributedLock(resourceName);
        }
        
        /**
         * 线程安全地增加计数器
         */
        public void increment() {
            try {
                lock.acquire();
                try {
                    counter++;
                    logger.info("计数器增加，当前值: {}, 线程: {}", 
                        counter, Thread.currentThread().getName());
                    Thread.sleep(100); // 模拟处理时间
                } finally {
                    lock.release();
                }
            } catch (Exception e) {
                logger.error("增加计数器失败", e);
            }
        }
        
        public int getCounter() {
            return counter;
        }
        
        public void close() throws IOException {
            lock.close();
        }
    }
    
    /**
     * 运行可重入锁示例
     */
    public static void runMutexDemo() throws Exception {
        logger.info("=== InterProcessMutex Demo ===");
        
        try (DistributedLock lock = new DistributedLock("test-mutex")) {
            
            // 演示可重入特性
            logger.info("演示可重入特性...");
            
            // 第一次获取锁
            lock.acquire();
            logger.info("第一次获取锁成功");
            
            // 同一个线程再次获取锁（可重入）
            lock.acquire();
            logger.info("同一个线程再次获取锁成功（可重入）");
            
            // 释放两次
            lock.release();
            logger.info("第一次释放锁");
            lock.release();
            logger.info("第二次释放锁");
        }
        
        logger.info("=== InterProcessMutex Demo 结束 ===");
    }
    
    /**
     * 运行多线程并发访问示例
     */
    public static void runConcurrentAccessDemo() throws Exception {
        logger.info("=== Concurrent Access Demo ===");
        
        try (SharedResource resource = new SharedResource("counter")) {
            
            // 创建多个线程并发访问
            Thread[] threads = new Thread[5];
            for (int i = 0; i < threads.length; i++) {
                final int threadNum = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 3; j++) {
                        resource.increment();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }, "Thread-" + threadNum);
            }
            
            // 启动所有线程
            for (Thread t : threads) {
                t.start();
            }
            
            // 等待所有线程完成
            for (Thread t : threads) {
                t.join();
            }
            
            logger.info("最终计数器值: {}", resource.getCounter());
        }
        
        logger.info("=== Concurrent Access Demo 结束 ===");
    }
    
    /**
     * 运行读写锁示例
     */
    public static void runReadWriteLockDemo() throws Exception {
        logger.info("=== InterProcessReadWriteLock Demo ===");
        
        try (DistributedReadWriteLock rwLock = new DistributedReadWriteLock("data-cache")) {
            
            // 获取读锁
            logger.info("获取读锁...");
            rwLock.readLock().acquire(5, TimeUnit.SECONDS);
            logger.info("读锁获取成功，可以并发读取");
            
            // 模拟读取操作
            Thread.sleep(1000);
            
            // 释放读锁
            rwLock.readLock().release();
            logger.info("读锁已释放");
            
            // 获取写锁
            logger.info("获取写锁...");
            rwLock.writeLock().acquire(5, TimeUnit.SECONDS);
            logger.info("写锁获取成功，独占访问");
            
            // 模拟写入操作
            Thread.sleep(1000);
            
            // 释放写锁
            rwLock.writeLock().release();
            logger.info("写锁已释放");
        }
        
        logger.info("=== InterProcessReadWriteLock Demo 结束 ===");
    }
    
    /**
     * 运行超时获取锁示例
     */
    public static void runTimeoutDemo() throws Exception {
        logger.info("=== Timeout Acquisition Demo ===");
        
        try (DistributedLock lock1 = new DistributedLock("exclusive");
             DistributedLock lock2 = new DistributedLock("exclusive")) {
            
            // 锁1 先获取锁
            lock1.acquire();
            logger.info("锁1 已获取锁");
            
            // 锁2 尝试获取锁（2秒超时）
            logger.info("锁2 尝试获取锁（2秒超时）...");
            boolean acquired = lock2.acquire(2, TimeUnit.SECONDS);
            
            if (acquired) {
                logger.info("锁2 获取锁成功");
                lock2.release();
            } else {
                logger.warn("锁2 获取锁超时");
            }
            
            // 释放锁1
            lock1.release();
            
            // 现在锁2 应该可以获取了
            lock2.acquire();
            logger.info("锁2 现在可以获取锁了");
            lock2.release();
        }
        
        logger.info("=== Timeout Acquisition Demo 结束 ===");
    }
    
    /**
     * 主方法：运行所有示例
     */
    public static void main(String[] args) throws Exception {
        try (EmbeddedZookeeper zkServer = new EmbeddedZookeeper(2181)) {
            logger.info("嵌入式 ZK 服务器已启动");
            
            // 运行可重入锁示例
            runMutexDemo();
            Thread.sleep(1000);
            
            // 运行并发访问示例
            runConcurrentAccessDemo();
            Thread.sleep(1000);
            
            // 运行读写锁示例
            runReadWriteLockDemo();
            Thread.sleep(1000);
            
            // 运行超时获取锁示例
            runTimeoutDemo();
            
        } catch (Exception e) {
            logger.error("示例执行失败", e);
        }
    }
}
