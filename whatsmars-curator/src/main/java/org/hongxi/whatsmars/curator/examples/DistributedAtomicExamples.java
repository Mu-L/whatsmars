package org.hongxi.whatsmars.curator.examples;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicValue;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.hongxi.whatsmars.curator.EmbeddedZookeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Curator Distributed Atomic Examples
 * 
 * Apache Curator 提供了多种分布式原子操作实现：
 * 1. DistributedAtomicLong - 分布式长整型原子数
 * 2. DistributedAtomicValue - 分布式值原子操作
 * 
 * 这些类使用 ZK 的原子性操作来保证分布式环境下的数据一致性。
 */
public class DistributedAtomicExamples {
    
    private static final Logger logger = LoggerFactory.getLogger(DistributedAtomicExamples.class);
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final String ATOMIC_PATH = "/curator/atomic";
    
    /**
     * 示例 1: DistributedAtomicLong - 分布式原子长整型
     * 
     * DistributedAtomicLong 提供了原子的增减操作，适用于：
     * 1. 分布式计数器
     * 2. 分布式 ID 生成器
     * 3. 限流器
     */
    public static class AtomicCounter implements Closeable {
        
        private final CuratorFramework client;
        private final DistributedAtomicLong counter;
        private final String name;
        
        public AtomicCounter(String name) {
            this.name = name;
            
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            // 创建 DistributedAtomicLong
            // 构造函数: client, path, retryPolicy
            counter = new DistributedAtomicLong(client, ATOMIC_PATH + "/" + name, 
                new RetryNTimes(3, 100));
        }
        
        /**
         * 增加计数（原子操作）
         * @param delta 增加的值
         * @return 操作结果
         */
        public AtomicValue<Long> increment(long delta) throws Exception {
            AtomicValue<Long> result = counter.add(delta);
            logger.info("{}: 增加 {}, 成功={}, 新值={}, 旧值={}", 
                name, delta, result.succeeded(), result.postValue(), result.preValue());
            return result;
        }
        
        /**
         * 减少计数（原子操作）
         * @param delta 减少的值
         * @return 操作结果
         */
        public AtomicValue<Long> decrement(long delta) throws Exception {
            AtomicValue<Long> result = counter.subtract(delta);
            logger.info("{}: 减少 {}, 成功={}, 新值={}, 旧值={}", 
                name, delta, result.succeeded(), result.postValue(), result.preValue());
            return result;
        }
        
        /**
         * 尝试设置为指定值
         * @param newValue 新值
         * @return 操作结果
         */
        public AtomicValue<Long> trySet(long newValue) throws Exception {
            AtomicValue<Long> result = counter.trySet(newValue);
            logger.info("{}: 尝试设置为 {}, 成功={}, 新值={}, 旧值={}", 
                name, newValue, result.succeeded(), result.postValue(), result.preValue());
            return result;
        }
        
        /**
         * 获取当前值
         */
        public Long getValue() throws Exception {
            AtomicValue<Long> value = counter.get();
            return value.postValue();
        }
        
        /**
         * 比较并设置（CAS 操作）
         * @param expectedValue 期望的旧值
         * @param newValue 新值
         * @return 操作结果
         */
        public boolean compareAndSet(long expectedValue, long newValue) throws Exception {
            AtomicValue<Long> result = counter.compareAndSet(expectedValue, newValue);
            logger.info("{}: CAS({}, {}) 成功={}, 结果={}", 
                name, expectedValue, newValue, result.succeeded(), result.postValue());
            return result.succeeded();
        }
        
        @Override
        public void close() throws IOException {
            client.close();
        }
    }
    
    /**
     * 示例 2: DistributedAtomicValue - 分布式原子值
     * 
     * DistributedAtomicValue 提供了原子的值更新操作，适用于：
     * 1. 分布式配置存储
     * 2. 分布式标志位
     * 3. 分布式版本号
     */
    public static class AtomicValueWrapper implements Closeable {
        
        private final CuratorFramework client;
        private final DistributedAtomicValue value;
        private final String name;
        
        public AtomicValueWrapper(String name) {
            this.name = name;
            
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            value = new DistributedAtomicValue(client, ATOMIC_PATH + "/value/" + name,
                new RetryNTimes(3, 100));
        }
        
        /**
         * 尝试设置为字节数组值
         */
        public void trySet(byte[] newValue) throws Exception {
            org.apache.curator.framework.recipes.atomic.AtomicValue<byte[]> result = 
                value.trySet(newValue);
            logger.info("{}: 设置成功, 成功={}", name, result.succeeded());
        }
        
        /**
         * 获取当前值
         */
        public String getValue() throws Exception {
            org.apache.curator.framework.recipes.atomic.AtomicValue<byte[]> v = value.get();
            return new String(v.postValue(), StandardCharsets.UTF_8);
        }
        
        /**
         * 比较并设置字节数组值（CAS 操作）
         */
        public boolean compareAndSet(byte[] expectedValue, byte[] newValue) throws Exception {
            org.apache.curator.framework.recipes.atomic.AtomicValue<byte[]> result = 
                value.compareAndSet(expectedValue, newValue);
            logger.info("{}: CAS 成功={}", name, result.succeeded());
            return result.succeeded();
        }
        
        @Override
        public void close() throws IOException {
            client.close();
        }
    }
    
    /**
     * 运行单线程计数器示例
     */
    public static void runSingleThreadCounterDemo() throws Exception {
        logger.info("=== Single Thread Counter Demo ===");
        
        try (AtomicCounter counter = new AtomicCounter("simple-counter")) {
            
            // 初始化为 0
            counter.trySet(0);
            
            // 多次增加
            for (int i = 0; i < 5; i++) {
                counter.increment(1);
                Thread.sleep(100);
            }
            
            // 获取当前值
            logger.info("当前计数: {}", counter.getValue());
            
            // 减少
            counter.decrement(2);
            
            // CAS 操作
            long currentValue = counter.getValue();
            counter.compareAndSet(currentValue, 100);
            
            logger.info("最终计数: {}", counter.getValue());
        }
        
        logger.info("=== Single Thread Counter Demo 结束 ===");
    }
    
    /**
     * 运行多线程并发计数器示例
     */
    public static void runMultiThreadCounterDemo() throws Exception {
        logger.info("=== Multi Thread Counter Demo ===");
        
        try (AtomicCounter counter = new AtomicCounter("concurrent-counter")) {
            
            // 初始化为 0
            counter.trySet(0);
            
            int threadCount = 5;
            int incrementsPerThread = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            // 启动多个线程同时增加计数
            for (int i = 0; i < threadCount; i++) {
                final int threadNum = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            counter.increment(1);
                            Thread.sleep(50);
                        }
                    } catch (Exception e) {
                        logger.error("线程 {} 出错", threadNum, e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // 等待所有线程完成
            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();
            
            // 获取最终值
            long finalValue = counter.getValue();
            long expectedValue = (long) threadCount * incrementsPerThread;
            
            logger.info("预期计数: {}, 实际计数: {}", expectedValue, finalValue);
            
            if (finalValue == expectedValue) {
                logger.info("✓ 计数器工作正常，所有操作已正确执行");
            } else {
                logger.warn("✗ 计数不匹配，可能有操作丢失");
            }
        }
        
        logger.info("=== Multi Thread Counter Demo 结束 ===");
    }
    
    /**
     * 运行分布式原子值示例
     */
    public static void runAtomicValueDemo() throws Exception {
        logger.info("=== Distributed Atomic Value Demo ===");
        
        try (AtomicValueWrapper atomicValue = new AtomicValueWrapper("config")) {
            
            // 设置初始值
            String initialValue = "version:1";
            atomicValue.trySet(initialValue.getBytes(StandardCharsets.UTF_8));
            logger.info("当前值: {}", atomicValue.getValue());
            
            // 获取当前值
            String currentValue = atomicValue.getValue();
            
            // CAS 操作
            String newValue = "version:2";
            atomicValue.compareAndSet(currentValue.getBytes(StandardCharsets.UTF_8), 
                                      newValue.getBytes(StandardCharsets.UTF_8));
            logger.info("更新后值: {}", atomicValue.getValue());
            
            // 再次尝试 CAS（会失败，因为当前值不是 version:1）
            boolean success = atomicValue.compareAndSet("version:1".getBytes(StandardCharsets.UTF_8), 
                                                        "version:3".getBytes(StandardCharsets.UTF_8));
            logger.info("CAS 结果: {}", success);
            logger.info("最终值: {}", atomicValue.getValue());
        }
        
        logger.info("=== Distributed Atomic Value Demo 结束 ===");
    }
    
    /**
     * 主方法：运行所有示例
     */
    public static void main(String[] args) throws Exception {
        try (EmbeddedZookeeper zkServer = new EmbeddedZookeeper(2181)) {
            logger.info("嵌入式 ZK 服务器已启动");
            
            // 运行单线程计数器示例
            runSingleThreadCounterDemo();
            Thread.sleep(1000);
            
            // 运行多线程计数器示例
            runMultiThreadCounterDemo();
            Thread.sleep(1000);
            
            // 运行原子值示例
            runAtomicValueDemo();
            
        } catch (Exception e) {
            logger.error("示例执行失败", e);
        }
    }
}
