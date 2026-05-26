package org.hongxi.whatsmars.curator.examples;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.x.discovery.*;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.hongxi.whatsmars.curator.EmbeddedZookeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * Curator Service Discovery Examples
 * 
 * Apache Curator 提供了服务发现功能，包括：
 * 1. ServiceInstance - 服务实例的定义
 * 2. ServiceDiscovery - 服务注册和发现
 * 3. ServiceCache - 服务缓存
 * 
 * 适用于微服务架构中的服务注册与发现场景。
 */
public class ServiceDiscoveryExamples {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryExamples.class);
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final String DISCOVERY_PATH = "/curator/discovery";
    
    /**
     * 服务实例的负载信息
     */
    public static class InstanceInfo {
        private String description;
        private int weight;
        
        public InstanceInfo() {}
        
        public InstanceInfo(String description, int weight) {
            this.description = description;
            this.weight = weight;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public int getWeight() {
            return weight;
        }
        
        public void setWeight(int weight) {
            this.weight = weight;
        }
        
        @Override
        public String toString() {
            return "InstanceInfo{description='" + description + "', weight=" + weight + "}";
        }
    }
    
    /**
     * 示例 1: 服务注册
     * 
     * 服务提供者将自己注册到 ZK，供服务消费者发现
     */
    public static class ServiceProvider implements Closeable {
        
        private final CuratorFramework client;
        private final ServiceDiscovery<InstanceInfo> discovery;
        private final ServiceInstance<InstanceInfo> instance;
        private final String serviceName;
        
        public ServiceProvider(String serviceName, String instanceId, String host, int port) throws Exception {
            this.serviceName = serviceName;
            
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            // 创建服务发现实例
            // JsonInstanceSerializer 用于序列化和反序列化服务实例信息
            discovery = ServiceDiscoveryBuilder.builder(InstanceInfo.class)
                .client(client)
                .basePath(DISCOVERY_PATH)
                .serializer(new JsonInstanceSerializer<>(InstanceInfo.class))
                .build();
            
            // 创建服务实例
            instance = ServiceInstance.<InstanceInfo>builder()
                .name(serviceName)
                .id(instanceId)
                .address(host)
                .port(port)
                .payload(new InstanceInfo("服务实例描述", 100))
                .build();
            
            // 启动服务发现
            discovery.start();
            
            // 注册服务
            discovery.registerService(instance);
            
            logger.info("服务已注册: name={}, id={}, address={}:{}", 
                serviceName, instanceId, host, port);
        }
        
        /**
         * 更新服务实例信息
         */
        public void updatePayload(InstanceInfo instanceInfo) throws Exception {
            ServiceInstance<InstanceInfo> updatedInstance = ServiceInstance.<InstanceInfo>builder()
                .name(serviceName)
                .id(instance.getId())
                .address(instance.getAddress())
                .port(instance.getPort())
                .payload(instanceInfo)
                .build();
            
            discovery.updateService(updatedInstance);
            logger.info("服务实例信息已更新: {}", instanceInfo);
        }
        
        @Override
        public void close() throws IOException {
            try {
                discovery.unregisterService(instance);
            } catch (Exception e) {
                logger.error("注销服务失败", e);
            }
            discovery.close();
            client.close();
        }
    }
    
    /**
     * 示例 2: 服务发现
     * 
     * 服务消费者从 ZK 发现可用的服务实例
     */
    public static class ServiceConsumer implements Closeable {
        
        private final CuratorFramework client;
        private final ServiceDiscovery<InstanceInfo> discovery;
        
        public ServiceConsumer() throws Exception {
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            discovery = ServiceDiscoveryBuilder.builder(InstanceInfo.class)
                .client(client)
                .basePath(DISCOVERY_PATH)
                .serializer(new JsonInstanceSerializer<>(InstanceInfo.class))
                .build();
            
            discovery.start();
            
            logger.info("服务消费者已启动");
        }
        
        /**
         * 查询所有服务实例
         */
        public Collection<ServiceInstance<InstanceInfo>> queryForInstances(String serviceName) throws Exception {
            return discovery.queryForInstances(serviceName);
        }
        
        /**
         * 查询单个服务实例
         */
        public ServiceInstance<InstanceInfo> queryForInstance(String serviceName) throws Exception {
            return discovery.queryForInstance(serviceName, null);
        }
        
        @Override
        public void close() throws IOException {
            discovery.close();
            client.close();
        }
    }
    
    /**
     * 运行服务注册示例
     */
    public static void runServiceRegistrationDemo() throws Exception {
        logger.info("=== Service Registration Demo ===");
        
        try (ServiceProvider provider = new ServiceProvider(
                "user-service", "instance-1", "192.168.1.100", 8080)) {
            
            // 等待一段时间，让服务注册生效
            Thread.sleep(5000);
            
            // 更新服务实例信息
            provider.updatePayload(new InstanceInfo("更新后的描述", 150));
            
            Thread.sleep(5000);
        }
        
        logger.info("=== Service Registration Demo 结束 ===");
    }
    
    /**
     * 运行服务发现示例
     */
    public static void runServiceDiscoveryDemo() throws Exception {
        logger.info("=== Service Discovery Demo ===");
        
        try (ServiceConsumer consumer = new ServiceConsumer()) {
            
            // 等待一下，让服务注册生效
            Thread.sleep(2000);
            
            // 查询所有服务实例
            Collection<ServiceInstance<InstanceInfo>> instances =
                consumer.queryForInstances("user-service");
            
            logger.info("发现 {} 个 'user-service' 实例:", instances.size());
            for (ServiceInstance<InstanceInfo> instance : instances) {
                logger.info("  - ID: {}, 地址: {}:{}", 
                    instance.getId(), 
                    instance.getAddress(), 
                    instance.getPort());
                if (instance.getPayload() != null) {
                    logger.info("    负载信息: {}", instance.getPayload());
                }
            }
        }
        
        logger.info("=== Service Discovery Demo 结束 ===");
    }
    
    /**
     * 运行完整的服务注册与发现流程
     */
    public static void runFullDemo() throws Exception {
        logger.info("=== Full Service Discovery Demo ===");
        
        // 启动多个服务提供者
        ServiceProvider provider1 = new ServiceProvider(
            "order-service", "order-1", "192.168.1.101", 8081);
        ServiceProvider provider2 = new ServiceProvider(
            "order-service", "order-2", "192.168.1.102", 8082);
        ServiceProvider provider3 = new ServiceProvider(
            "payment-service", "payment-1", "192.168.1.103", 8083);
        
        // 等待服务注册
        Thread.sleep(3000);
        
        // 启动消费者
        try (ServiceConsumer consumer = new ServiceConsumer()) {
            
            // 等待缓存初始化
            Thread.sleep(2000);
            
            // 发现 order-service
            logger.info("\n发现 order-service:");
            Collection<ServiceInstance<InstanceInfo>> orderInstances =
                consumer.queryForInstances("order-service");
            for (ServiceInstance<InstanceInfo> instance : orderInstances) {
                logger.info("  - {}:{} ({})", 
                    instance.getAddress(), 
                    instance.getPort(),
                    instance.getId());
            }
            
            // 发现 payment-service
            logger.info("\n发现 payment-service:");
            Collection<ServiceInstance<InstanceInfo>> paymentInstances =
                consumer.queryForInstances("payment-service");
            for (ServiceInstance<InstanceInfo> instance : paymentInstances) {
                logger.info("  - {}:{} ({})", 
                    instance.getAddress(), 
                    instance.getPort(),
                    instance.getId());
            }
        } finally {
            // 关闭服务提供者
            provider1.close();
            provider2.close();
            provider3.close();
        }
        
        logger.info("=== Full Service Discovery Demo 结束 ===");
    }
    
    /**
     * 主方法：运行所有示例
     */
    public static void main(String[] args) throws Exception {
        try (EmbeddedZookeeper zkServer = new EmbeddedZookeeper(2181)) {
            logger.info("嵌入式 ZK 服务器已启动");
            
            // 运行完整的服务发现流程
            runFullDemo();
            
        } catch (Exception e) {
            logger.error("示例执行失败", e);
        }
    }
}
