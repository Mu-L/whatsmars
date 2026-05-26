package org.hongxi.whatsmars.curator.examples;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.hongxi.whatsmars.curator.EmbeddedZookeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Curator Framework Basic Operations Examples
 *
 * 本示例展示 Apache Curator Framework 的基本操作：
 * 1. 节点的增删改查
 * 2. 节点的异步操作
 * 3. 节点的监听（Watcher）
 * 4. 使用 CuratorCache 监听节点变化
 */
public class CuratorFrameworkExamples {

    private static final Logger logger = LoggerFactory.getLogger(CuratorFrameworkExamples.class);
    private static final String ZK_ADDRESS = "127.0.0.1:2181";
    private static final String BASE_PATH = "/curator/demo";

    /**
     * 运行所有示例
     */
    public static void main(String[] args) throws Exception {
        try (EmbeddedZookeeper zkServer = new EmbeddedZookeeper(2181)) {
            logger.info("嵌入式 ZK 服务器已启动");

            // 创建 Curator 客户端
            try (CuratorFramework client = createClient()) {
                client.start();
                logger.info("Curator 客户端已启动");

                // 运行基本 CRUD 操作示例
                runCRUDOperations(client);
                Thread.sleep(1000);

                // 运行异步操作示例
                runAsyncOperations(client);
                Thread.sleep(1000);

                // 运行 Watcher 示例
                runWatcherExample(client);
                Thread.sleep(1000);

                // 运行 CuratorCache 示例
                runCuratorCacheExample(client);

            } catch (Exception e) {
                logger.error("示例执行失败", e);
            }
        }
    }

    /**
     * 创建 Curator 客户端
     */
    private static CuratorFramework createClient() {
        return CuratorFrameworkFactory.builder()
                .connectString(ZK_ADDRESS)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .namespace("demo")
                .build();
    }

    // ==================== 节点 CRUD 操作 ====================

    /**
     * 运行基本 CRUD 操作示例
     */
    private static void runCRUDOperations(CuratorFramework client) throws Exception {
        logger.info("=== Curator CRUD Operations Demo ===");

        String path = BASE_PATH + "/node1";

        // 确保父节点存在
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .forPath(BASE_PATH);
            logger.info("确保父节点存在: {}", BASE_PATH);
        } catch (Exception e) {
            // 忽略节点已存在的错误
        }

        // CREATE - 创建节点
        logger.info("\n--- CREATE ---");

        // 创建持久节点
        try {
            String nodePath = client.create()
                    .withMode(CreateMode.PERSISTENT)
                    .forPath(path, "Hello Curator".getBytes(StandardCharsets.UTF_8));
            logger.info("创建节点: {}, 数据: Hello Curator", nodePath);
        } catch (Exception e) {
            logger.warn("创建节点失败（可能已存在）: {}", e.getMessage());
            // 如果节点已存在，先删除再创建
            try {
                client.delete().forPath(path);
                client.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path, "Hello Curator".getBytes(StandardCharsets.UTF_8));
                logger.info("重建节点: {}", path);
            } catch (Exception e2) {
                logger.error("重建节点失败", e2);
            }
        }

        // 创建持久顺序节点
        String sequentialPath = client.create()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .forPath(BASE_PATH + "/sequential-", "sequential data".getBytes(StandardCharsets.UTF_8));
        logger.info("创建顺序节点: {}", sequentialPath);

        // 创建临时节点
        String ephemeralPath = client.create()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(BASE_PATH + "/ephemeral", "temporary data".getBytes(StandardCharsets.UTF_8));
        logger.info("创建临时节点: {}, 数据: temporary data", ephemeralPath);

        // 创建多层节点（自动创建父节点）
        String multiLevelPath = client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(BASE_PATH + "/level1/level2/level3", "multi-level data".getBytes(StandardCharsets.UTF_8));
        logger.info("创建多层节点: {}", multiLevelPath);

        // READ - 读取节点
        logger.info("\n--- READ ---");

        // 检查节点是否存在
        Stat existsStat = client.checkExists()
                .forPath(path);
        logger.info("节点 {} 是否存在: {}", path, existsStat != null);

        if (existsStat == null) {
            logger.warn("节点不存在，跳过读取操作");
        } else {
            // 读取节点数据
            byte[] data = client.getData()
                    .forPath(path);
            logger.info("读取节点 {} 数据: {}", path, new String(data, StandardCharsets.UTF_8));

            // 读取节点数据（带状态信息）
            Stat stat = new Stat();
            data = client.getData()
                    .storingStatIn(stat)
                    .forPath(path);
            logger.info("读取节点 {} 数据: {}, 版本: {}", path, new String(data, StandardCharsets.UTF_8), stat.getVersion());
        }

        // 列出子节点
        String parentPath = BASE_PATH + "/level1";
        client.create()
                .creatingParentsIfNeeded()
                .forPath(parentPath + "/child1", "child1 data".getBytes(StandardCharsets.UTF_8));
        client.create()
                .forPath(parentPath + "/child2", "child2 data".getBytes(StandardCharsets.UTF_8));

        List<String> children = client.getChildren()
                .forPath(parentPath);
        logger.info("节点 {} 的子节点: {}", parentPath, children);

        // UPDATE - 更新节点
        logger.info("\n--- UPDATE ---");

        if (existsStat != null) {
            // 更新节点数据
            Stat updateStat = client.setData()
                    .forPath(path, "Updated by Curator".getBytes(StandardCharsets.UTF_8));
            logger.info("更新节点 {} 数据，新版本: {}", path, updateStat.getVersion());

            // 读取验证
            byte[] updatedData = client.getData().forPath(path);
            logger.info("验证更新后数据: {}", new String(updatedData, StandardCharsets.UTF_8));

            // 使用 CAS 更新（乐观锁）
            try {
                // 使用错误的版本号更新，会失败
                client.setData()
                        .withVersion(0)  // 使用旧版本号
                        .forPath(path, "Should fail".getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                logger.info("CAS 更新失败（预期），因为版本号不匹配: {}", e.getMessage());
            }
        } else {
            logger.info("跳过 UPDATE 操作（节点不存在）");
        }

        // DELETE - 删除节点
        logger.info("\n--- DELETE ---");

        // 删除单个节点
        try {
            client.delete()
                    .forPath(path);
            logger.info("删除节点: {}", path);
        } catch (Exception e) {
            logger.warn("删除节点失败（可能不存在）: {}", e.getMessage());
        }

        // 删除节点（不存在不报错）
        try {
            client.delete()
                    .guaranteed()
                    .forPath("/non-existent-node");
            logger.info("删除不存在的节点（guaranteed 模式）");
        } catch (Exception e) {
            logger.warn("guaranteed 删除失败: {}", e.getMessage());
        }

        // 递归删除节点及其子节点
        try {
            client.delete()
                    .guaranteed()
                    .deletingChildrenIfNeeded()
                    .forPath(BASE_PATH);
            logger.info("递归删除节点: {} 及其所有子节点", BASE_PATH);
        } catch (Exception e) {
            logger.warn("递归删除失败: {}", e.getMessage());
        }

        logger.info("=== Curator CRUD Operations Demo 结束 ===");
    }

    // ==================== 异步操作 ====================

    /**
     * 运行异步操作示例
     */
    private static void runAsyncOperations(CuratorFramework client) throws Exception {
        logger.info("=== Curator Async Operations Demo ===");

        String path = BASE_PATH + "/async-node";

        // 创建节点用于测试
        client.create()
                .creatingParentsIfNeeded()
                .forPath(path, "async data".getBytes(StandardCharsets.UTF_8));

        CountDownLatch latch = new CountDownLatch(3);

        // 异步创建节点
        client.create()
                .withMode(CreateMode.PERSISTENT)
                .inBackground((framework, event) -> {
                    logger.info("异步创建回调 - 类型: {}, 路径: {}, 结果码: {}",
                            event.getType(), event.getPath(), event.getResultCode());
                    latch.countDown();
                })
                .forPath(BASE_PATH + "/async-create", "created async".getBytes(StandardCharsets.UTF_8));

        // 异步读取节点
        client.getData()
                .inBackground((framework, event) -> {
                    if (event.getData() != null) {
                        logger.info("异步读取回调 - 数据: {}",
                                new String(event.getData(), StandardCharsets.UTF_8));
                    } else {
                        logger.info("异步读取回调 - 路径: {}, 结果: {}",
                                event.getPath(), event.getResultCode());
                    }
                    latch.countDown();
                })
                .forPath(path);

        // 异步更新节点
        client.setData()
                .inBackground((framework, event) -> {
                    logger.info("异步更新回调 - 路径: {}, 结果码: {}",
                            event.getPath(), event.getResultCode());
                    latch.countDown();
                })
                .forPath(path, "updated async".getBytes(StandardCharsets.UTF_8));

        // 等待异步操作完成
        latch.await(5, TimeUnit.SECONDS);

        // 使用 AsyncCallback
        logger.info("\n--- Using AsyncCallback ---");
        CountDownLatch latch2 = new CountDownLatch(1);

        client.create()
                .withMode(CreateMode.PERSISTENT)
                .inBackground((CuratorFramework framework, CuratorEvent event) -> {
                    logger.info("Callback 收到事件 - Type: {}, Path: {}, ResultCode: {}",
                            event.getType(), event.getPath(), event.getResultCode());
                    latch2.countDown();
                })
                .forPath(BASE_PATH + "/callback-node", "callback data".getBytes(StandardCharsets.UTF_8));

        latch2.await(5, TimeUnit.SECONDS);

        logger.info("=== Curator Async Operations Demo 结束 ===");
    }

    // ==================== Watcher 操作 ====================

    /**
     * 运行 Watcher 示例
     */
    private static void runWatcherExample(CuratorFramework client) throws Exception {
        logger.info("=== Curator Watcher Demo ===");

        String path = BASE_PATH + "/watched-node";

        // 确保父节点存在并创建测试节点
        try {
            client.create()
                    .creatingParentsIfNeeded()
                    .forPath(path, "initial data".getBytes(StandardCharsets.UTF_8));
            logger.info("创建测试节点: {}", path);
        } catch (Exception e) {
            logger.warn("创建节点失败（可能已存在）: {}", e.getMessage());
            // 更新现有节点数据
            client.setData()
                    .forPath(path, "initial data".getBytes(StandardCharsets.UTF_8));
        }

        CountDownLatch latch = new CountDownLatch(3);

        // 添加节点数据变化的监听器
        Watcher watcher = event -> {
            logger.info("Watcher 事件 - 类型: {}, 路径: {}, 状态: {}",
                    event.getType(), event.getPath(), event.getState());
            latch.countDown();
        };

        // 使用 usingWatcher 添加一次性 watcher
        logger.info("添加 one-time watcher...");
        try {
            client.getData()
                    .usingWatcher(watcher)
                    .inBackground()
                    .forPath(path);
        } catch (Exception e) {
            logger.warn("添加 watcher 失败: {}", e.getMessage());
        }

        // 触发事件 - 更新节点
        client.setData()
                .forPath(path, "data 1".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(100);

        // 再添加一个 watcher（用于测试）
        logger.info("再添加一个 one-time watcher...");
        Watcher watcher2 = event -> {
            logger.info("第二个 Watcher 事件 - 类型: {}, 路径: {}",
                    event.getType(), event.getPath());
            latch.countDown();
        };
        client.getData()
                .usingWatcher(watcher2)
                .inBackground()
                .forPath(path);

        // 再次更新触发事件
        client.setData()
                .forPath(path, "data 2".getBytes(StandardCharsets.UTF_8));

        // 等待事件触发
        latch.await(3, TimeUnit.SECONDS);

        // 清理
        try {
            client.delete()
                    .deletingChildrenIfNeeded()
                    .forPath(BASE_PATH);
        } catch (Exception e) {
            logger.warn("清理失败: {}", e.getMessage());
        }

        logger.info("=== Curator Watcher Demo 结束 ===");
    }

    // ==================== CuratorCache 操作 ====================

    /**
     * 运行 CuratorCache 示例
     */
    private static void runCuratorCacheExample(CuratorFramework client) throws Exception {
        logger.info("=== CuratorCache Demo ===");

        String cachePath = BASE_PATH + "/cache";

        // 创建 CuratorCache
        try (CuratorCache cache = CuratorCache.build(client, cachePath)) {

            // 添加监听器 - 使用 curator 5.x API
            CuratorCacheListener listener = (type, childData, oldData) -> {
                switch (type) {
                    case NODE_CREATED:
                        logger.info("[Cache] 节点创建: {}", childData != null ? childData.getPath() : "unknown");
                        break;
                    case NODE_CHANGED:
                        logger.info("[Cache] 节点变更: {} -> {}",
                                oldData != null ? new String(oldData.getData(), StandardCharsets.UTF_8) : "null",
                                childData != null ? new String(childData.getData(), StandardCharsets.UTF_8) : "null");
                        break;
                    case NODE_DELETED:
                        logger.info("[Cache] 节点删除: {}", childData != null ? childData.getPath() : "unknown");
                        break;
                }
            };

            cache.listenable().addListener(listener);

            // 启动缓存
            cache.start();
            logger.info("CuratorCache 已启动");

            // 创建节点
            try {
                client.create()
                        .creatingParentsIfNeeded()
                        .forPath(cachePath + "/node1", "data1".getBytes(StandardCharsets.UTF_8));
                logger.info("创建节点: {}/node1", cachePath);
            } catch (Exception e) {
                logger.warn("创建节点失败: {}", e.getMessage());
            }
            Thread.sleep(100);

            // 创建子节点
            try {
                client.create()
                        .forPath(cachePath + "/node2", "data2".getBytes(StandardCharsets.UTF_8));
                logger.info("创建节点: {}/node2", cachePath);
            } catch (Exception e) {
                logger.warn("创建节点失败: {}", e.getMessage());
            }
            Thread.sleep(100);

            // 更新节点
            try {
                client.setData()
                        .forPath(cachePath + "/node1", "updated data1".getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                logger.warn("更新节点失败: {}", e.getMessage());
            }
            Thread.sleep(100);

            // 删除节点
            try {
                client.delete()
                        .forPath(cachePath + "/node2");
            } catch (Exception e) {
                logger.warn("删除节点失败: {}", e.getMessage());
            }
            Thread.sleep(100);

            // 等待事件处理
            Thread.sleep(500);

            // 打印缓存内容
            logger.info("\n--- CuratorCache 已启动，节点操作已完成 ---");

        }

        // 清理
        try {
            client.delete()
                    .deletingChildrenIfNeeded()
                    .forPath(BASE_PATH);
        } catch (Exception e) {
            logger.warn("清理失败: {}", e.getMessage());
        }

        logger.info("=== CuratorCache Demo 结束 ===");
    }
}
