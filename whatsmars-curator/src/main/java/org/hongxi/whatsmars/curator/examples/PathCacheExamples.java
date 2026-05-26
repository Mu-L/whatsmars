package org.hongxi.whatsmars.curator.examples;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.hongxi.whatsmars.curator.EmbeddedZookeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Curator Path Cache Examples
 * 
 * Apache Curator 提供了三种缓存实现：
 * 1. NodeCache - 缓存单个节点的数据和状态
 * 2. PathChildrenCache - 缓存子节点列表和状态
 * 3. TreeCache - 缓存节点树（NodeCache + PathChildrenCache）
 */
public class PathCacheExamples {
    
    private static final Logger logger = LoggerFactory.getLogger(PathCacheExamples.class);
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final String CACHE_PATH = "/curator/cache";
    
    /**
     * 示例 1: NodeCache - 监控单个节点
     * 
     * NodeCache 用于监控 ZK 树中的单个节点，可以：
     * 1. 获取节点的当前数据
     * 2. 监听节点数据的变化
     * 3. 监听节点的创建和删除
     */
    public static class NodeCacheExample implements AutoCloseable {
        
        private final CuratorFramework client;
        private final NodeCache nodeCache;
        private final String path;
        
        public NodeCacheExample(String path) throws Exception {
            this.path = path;
            
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            // 创建 NodeCache
            nodeCache = new NodeCache(client, path, false);
            
            // 添加监听器
            nodeCache.getListenable().addListener(() -> {
                ChildData currentData = nodeCache.getCurrentData();
                if (currentData != null) {
                    String data = new String(currentData.getData(), StandardCharsets.UTF_8);
                    logger.info("节点数据变更: path={}, data={}, stat={}", 
                        currentData.getPath(), data, currentData.getStat());
                } else {
                    logger.info("节点已删除: {}", path);
                }
            });
            
            // 启动缓存
            nodeCache.start(true);
            
            logger.info("NodeCache 已启动，监控路径: {}", path);
        }
        
        /**
         * 更新节点数据
         */
        public void updateData(String data) throws Exception {
            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {
                // 节点不存在，创建它
                client.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path, data.getBytes(StandardCharsets.UTF_8));
                logger.info("创建节点: {}, data: {}", path, data);
            } else {
                // 节点已存在，更新数据
                client.setData().forPath(path, data.getBytes(StandardCharsets.UTF_8));
                logger.info("更新节点数据: {}, data: {}", path, data);
            }
        }
        
        /**
         * 获取当前缓存的数据
         */
        public String getCurrentData() {
            ChildData currentData = nodeCache.getCurrentData();
            if (currentData != null) {
                return new String(currentData.getData(), StandardCharsets.UTF_8);
            }
            return null;
        }
        
        /**
         * 关闭资源
         */
        public void close() throws Exception {
            nodeCache.close();
            client.close();
        }
    }
    
    /**
     * 示例 2: PathChildrenCache - 监控子节点
     * 
     * PathChildrenCache 用于监控某个节点的所有子节点，可以：
     * 1. 获取所有子节点的列表
     * 2. 监听子节点的增删改
     * 3. 监听子节点数据的变化
     */
    public static class PathChildrenCacheExample implements AutoCloseable {
        
        private final CuratorFramework client;
        private final PathChildrenCache cache;
        private final String path;
        
        public PathChildrenCacheExample(String path) throws Exception {
            this.path = path;
            
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            // 创建 PathChildrenCache
            // 第三个参数：是否缓存数据
            cache = new PathChildrenCache(client, path, true);
            
            // 添加监听器
            cache.getListenable().addListener((curatorClient, event) -> {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        logger.info("子节点添加: {}, data={}", 
                            event.getData().getPath(), 
                            new String(event.getData().getData(), StandardCharsets.UTF_8));
                        break;
                    case CHILD_UPDATED:
                        logger.info("子节点更新: {}, data={}", 
                            event.getData().getPath(), 
                            new String(event.getData().getData(), StandardCharsets.UTF_8));
                        break;
                    case CHILD_REMOVED:
                        logger.info("子节点删除: {}", event.getData().getPath());
                        break;
                    case INITIALIZED:
                        logger.info("PathChildrenCache 初始化完成");
                        break;
                    default:
                        logger.info("其他事件: {}", event.getType());
                }
            });
            
            // 启动缓存 - NORMAL: 异步初始化
            // POST_INITIALIZED_EVENT: 初始化完成后发送事件
            cache.start(StartMode.POST_INITIALIZED_EVENT);
            
            logger.info("PathChildrenCache 已启动，监控路径: {}", path);
        }
        
        /**
         * 添加子节点
         */
        public void addChild(String childName, String data) throws Exception {
            String childPath = ZKPaths.makePath(path, childName);
            client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(childPath, data.getBytes(StandardCharsets.UTF_8));
            logger.info("添加子节点: {}, data: {}", childPath, data);
        }
        
        /**
         * 更新子节点
         */
        public void updateChild(String childName, String data) throws Exception {
            String childPath = ZKPaths.makePath(path, childName);
            client.setData().forPath(childPath, data.getBytes(StandardCharsets.UTF_8));
            logger.info("更新子节点: {}, data: {}", childPath, data);
        }
        
        /**
         * 删除子节点
         */
        public void deleteChild(String childName) throws Exception {
            String childPath = ZKPaths.makePath(path, childName);
            client.delete().forPath(childPath);
            logger.info("删除子节点: {}", childPath);
        }
        
        /**
         * 获取所有当前子节点
         */
        public List<ChildData> getCurrentChildren() {
            return cache.getCurrentData();
        }
        
        /**
         * 关闭资源
         */
        public void close() throws Exception {
            cache.close();
            client.close();
        }
    }
    
    /**
     * 示例 3: TreeCache - 监控节点树
     * 
     * TreeCache 结合了 NodeCache 和 PathChildrenCache 的功能，
     * 可以监控整个节点树的变化。
     */
    public static class TreeCacheExample implements AutoCloseable {
        
        private final CuratorFramework client;
        private final TreeCache cache;
        private final String path;
        
        public TreeCacheExample(String path) throws Exception {
            this.path = path;
            
            client = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
            );
            client.start();
            
            // 创建 TreeCache
            cache = new TreeCache(client, path);
            
            // 添加监听器
            cache.getListenable().addListener((curatorClient, event) -> {
                switch (event.getType()) {
                    case NODE_ADDED:
                        logger.info("节点添加: {}, data={}", 
                            event.getData().getPath(), 
                            new String(event.getData().getData(), StandardCharsets.UTF_8));
                        break;
                    case NODE_UPDATED:
                        logger.info("节点更新: {}, data={}", 
                            event.getData().getPath(), 
                            new String(event.getData().getData(), StandardCharsets.UTF_8));
                        break;
                    case NODE_REMOVED:
                        logger.info("节点删除: {}", event.getData().getPath());
                        break;
                    default:
                        logger.info("其他事件: {}", event.getType());
                }
            });
            
            // 启动缓存
            cache.start();
            
            logger.info("TreeCache 已启动，监控路径: {}", path);
        }
        
        /**
         * 创建节点树
         */
        public void createTree() throws Exception {
            if (client.checkExists().forPath(path + "/level1/level2/level3") != null) {
                client.delete().deletingChildrenIfNeeded().forPath(path + "/level1/level2/level3");
            }
            client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(path + "/level1/level2/level3", "root".getBytes(StandardCharsets.UTF_8));
            logger.info("创建节点树: {}/level1/level2/level3", path);
        }
        
        /**
         * 更新节点
         */
        public void updateNode(String nodePath, String data) throws Exception {
            client.setData().forPath(nodePath, data.getBytes(StandardCharsets.UTF_8));
            logger.info("更新节点: {}, data: {}", nodePath, data);
        }
        
        /**
         * 获取当前树中的所有节点
         */
        public void printTree() {
            logger.info("当前缓存的节点树:");
            cache.getCurrentChildren(path).forEach((name, data) -> {
                logger.info("  {} = {}", name, data.getPath());
            });
        }
        
        /**
         * 关闭资源
         */
        public void close() throws Exception {
            cache.close();
            client.close();
        }
    }
    
    /**
     * 运行 NodeCache 示例
     */
    public static void runNodeCacheDemo() throws Exception {
        logger.info("=== NodeCache Demo ===");
        
        String path = CACHE_PATH + "/node";
        try (NodeCacheExample cache = new NodeCacheExample(path)) {
            
            // 等待缓存初始化
            Thread.sleep(1000);
            
            // 创建节点
            cache.updateData("initial data");
            Thread.sleep(500);
            
            // 更新节点
            cache.updateData("updated data");
            Thread.sleep(500);
            
            // 获取当前数据
            logger.info("当前缓存数据: {}", cache.getCurrentData());
            
            Thread.sleep(2000);
        }
        
        logger.info("=== NodeCache Demo 结束 ===");
    }
    
    /**
     * 运行 PathChildrenCache 示例
     */
    public static void runPathChildrenCacheDemo() throws Exception {
        logger.info("=== PathChildrenCache Demo ===");
        
        String path = CACHE_PATH + "/children";
        try (PathChildrenCacheExample cache = new PathChildrenCacheExample(path)) {
            
            // 等待缓存初始化
            Thread.sleep(1000);
            
            // 添加子节点
            cache.addChild("child1", "data1");
            Thread.sleep(500);
            
            cache.addChild("child2", "data2");
            Thread.sleep(500);
            
            cache.addChild("child3", "data3");
            Thread.sleep(500);
            
            // 获取当前所有子节点
            logger.info("当前子节点数: {}", cache.getCurrentChildren().size());
            
            // 更新子节点
            cache.updateChild("child1", "updated data1");
            Thread.sleep(500);
            
            // 删除子节点
            cache.deleteChild("child2");
            Thread.sleep(500);
            
            Thread.sleep(2000);
        }
        
        logger.info("=== PathChildrenCache Demo 结束 ===");
    }
    
    /**
     * 运行 TreeCache 示例
     */
    public static void runTreeCacheDemo() throws Exception {
        logger.info("=== TreeCache Demo ===");
        
        try (TreeCacheExample cache = new TreeCacheExample(CACHE_PATH)) {
            
            // 等待缓存初始化
            Thread.sleep(1000);
            
            // 创建节点树
            cache.createTree();
            Thread.sleep(1000);
            
            // 更新节点
            cache.updateNode(CACHE_PATH + "/level1", "level1 data");
            Thread.sleep(500);
            
            // 打印树
            cache.printTree();
            
            Thread.sleep(2000);
        }
        
        logger.info("=== TreeCache Demo 结束 ===");
    }
    
    /**
     * 主方法：运行所有示例
     */
    public static void main(String[] args) throws Exception {
        try (EmbeddedZookeeper zkServer = new EmbeddedZookeeper(2181)) {
            logger.info("嵌入式 ZK 服务器已启动");
            
            // 运行 NodeCache 示例
            runNodeCacheDemo();
            Thread.sleep(1000);
            
            // 运行 PathChildrenCache 示例
            runPathChildrenCacheDemo();
            Thread.sleep(1000);
            
            // 运行 TreeCache 示例
            runTreeCacheDemo();
            
        } catch (Exception e) {
            logger.error("示例执行失败", e);
        }
    }
}
