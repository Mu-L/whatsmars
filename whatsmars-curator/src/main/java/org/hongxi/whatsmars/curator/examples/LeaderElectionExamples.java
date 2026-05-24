package org.hongxi.whatsmars.curator.examples;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.hongxi.whatsmars.curator.EmbeddedZookeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Curator Leader Election Examples
 * 
 * Apache Curator 提供了 LeaderSelector 进行领导选举。
 * LeaderSelector 允许客户端竞争领导权，当获得领导权后可以执行业务逻辑，
 * 然后释放领导权让其他客户端有机会成为领导者。
 */
public class LeaderElectionExamples {
    
    private static final Logger logger = LoggerFactory.getLogger(LeaderElectionExamples.class);
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final String ELECTION_PATH = "/curator/election";
    
    /**
     * 使用 LeaderSelector 进行领导选举
     * 
     * LeaderSelector 核心特点：
     * 1. 当获得领导权时，会回调 takeLeadership 方法
     * 2. 执行完业务逻辑后，释放领导权，其他客户端可以竞争
     * 3. 支持自动重新参与选举（autoRequeue）
     * 4. 可以设置领导权获取的优先级
     */
    public static class LeaderSelectorExample implements Closeable {
        
        private final CuratorFramework client;
        private final LeaderSelector leaderSelector;
        private final String clientName;
        private volatile boolean isLeader = false;
        private CountDownLatch leadershipLatch = new CountDownLatch(1);
        
        public LeaderSelectorExample(String clientName) {
            this.clientName = clientName;
            
            // 创建 Curator 客户端
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            // 创建 LeaderSelector
            leaderSelector = new LeaderSelector(
                client, 
                ELECTION_PATH, 
                new LeaderSelectorListenerAdapter() {
                    @Override
                    public void takeLeadership(CuratorFramework client) throws Exception {
                        // 当获得领导权时，会调用此方法
                        isLeader = true;
                        leadershipLatch.countDown();
                        logger.info("{} 成为领导者!", clientName);
                        
                        try {
                            // 模拟执行领导职责
                            // 在实际应用中，这里可以执行：
                            // - 定时任务调度
                            // - 监控和检查
                            // - 配置更新分发
                            // - 其他需要单一节点执行的任务
                            Thread.sleep(5000);
                        } finally {
                            isLeader = false;
                            leadershipLatch = new CountDownLatch(1);
                            logger.info("{} 放弃领导权", clientName);
                        }
                    }
                }
            );
            
            // 自动重新参与选举 - 当放弃领导权后自动重新加入选举队列
            leaderSelector.autoRequeue();
        }
        
        public void start() {
            leaderSelector.start();
            logger.info("{} 开始参与选举", clientName);
        }
        
        /**
         * 等待获得领导权
         * @param timeout 超时时间
         * @param unit 时间单位
         * @return 是否获得领导权
         */
        public boolean awaitLeadership(long timeout, TimeUnit unit) throws InterruptedException {
            return leadershipLatch.await(timeout, unit);
        }
        
        public boolean hasLeadership() {
            return isLeader;
        }
        
        public String getClientName() {
            return clientName;
        }
        
        @Override
        public void close() throws IOException {
            leaderSelector.close();
            client.close();
        }
    }
    
    /**
     * 运行 LeaderSelector 示例
     * 启动多个客户端，观察领导权的转移
     */
    public static void runLeaderSelectorDemo() throws Exception {
        logger.info("=== LeaderSelector Demo ===");
        
        try (LeaderSelectorExample client1 = new LeaderSelectorExample("Client1");
             LeaderSelectorExample client2 = new LeaderSelectorExample("Client2");
             LeaderSelectorExample client3 = new LeaderSelectorExample("Client3")) {
            
            client1.start();
            client2.start();
            client3.start();
            
            // 等待所有客户端都参与选举
            Thread.sleep(1000);
            
            // 运行 15 秒观察领导权变化
            // 领导权会每 5 秒在客户端之间转移
            for (int i = 0; i < 3; i++) {
                logger.info("当前状态: Client1领导={}, Client2领导={}, Client3领导={}", 
                    client1.hasLeadership(), 
                    client2.hasLeadership(), 
                    client3.hasLeadership());
                Thread.sleep(5000);
            }
        }
        
        logger.info("=== LeaderSelector Demo 结束 ===");
    }
    
    /**
     * 运行单一客户端示例
     * 观察客户端获得和释放领导权
     */
    public static void runSingleClientDemo() throws Exception {
        logger.info("=== Single Client Demo ===");
        
        try (LeaderSelectorExample client = new LeaderSelectorExample("SingleClient")) {
            client.start();
            
            // 等待获得领导权
            if (client.awaitLeadership(10, TimeUnit.SECONDS)) {
                logger.info("SingleClient 成功获得领导权");
                
                // 执行任务
                Thread.sleep(3000);
                
                logger.info("SingleClient 释放领导权");
            } else {
                logger.warn("SingleClient 未能获得领导权");
            }
        }
        
        logger.info("=== Single Client Demo 结束 ===");
    }
    
    /**
     * 使用嵌入式 ZK 服务器运行示例
     */
    public static void main(String[] args) throws Exception {
        try (EmbeddedZookeeper zkServer = new EmbeddedZookeeper(2181)) {
            logger.info("嵌入式 ZK 服务器已启动");
            
            // 运行单一客户端示例
            runSingleClientDemo();
            
            Thread.sleep(1000);
            
            // 运行多客户端示例
            runLeaderSelectorDemo();
            
        } catch (Exception e) {
            logger.error("示例执行失败", e);
        }
    }
}
