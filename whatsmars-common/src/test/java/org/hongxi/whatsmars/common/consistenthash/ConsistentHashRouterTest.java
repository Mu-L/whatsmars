package org.hongxi.whatsmars.common.consistenthash;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 一致性哈希（ConsistentHashRouter）演示与单元测试
 *
 * 演示场景：
 * 1. 基础路由：同一 key 总是路由到同一节点
 * 2. 负载均衡：虚拟节点使数据分布更均匀
 * 3. 节点增减：增删节点时只有少量 key 重新映射
 * 4. 空环处理：无节点时返回 null
 */
public class ConsistentHashRouterTest {

    /**
     * 简单的物理节点实现
     */
    static class ServerNode implements Node {
        private final String ip;

        ServerNode(String ip) {
            this.ip = ip;
        }

        @Override
        public String getKey() {
            return ip;
        }

        @Override
        public String toString() {
            return ip;
        }
    }

    // ==================== 基础路由 ====================

    @Test
    public void testRouteNode_sameKeyRoutesToSameNode() {
        List<ServerNode> nodes = List.of(
                new ServerNode("192.168.1.1"),
                new ServerNode("192.168.1.2"),
                new ServerNode("192.168.1.3")
        );
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(nodes, 100);

        // 同一个 key 多次路由应返回同一个节点
        ServerNode first = router.routeNode("user:1001");
        for (int i = 0; i < 100; i++) {
            assertSame(first, router.routeNode("user:1001"),
                    "相同 key 应始终路由到相同节点");
        }
    }

    @Test
    public void testRouteNode_differentKeysMayRouteToDifferentNodes() {
        List<ServerNode> nodes = List.of(
                new ServerNode("192.168.1.1"),
                new ServerNode("192.168.1.2"),
                new ServerNode("192.168.1.3")
        );
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(nodes, 100);

        Set<String> routedNodes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ServerNode node = router.routeNode("key:" + i);
            routedNodes.add(node.getKey());
        }

        // 1000 个不同的 key 应该覆盖所有 3 个节点
        assertEquals(3, routedNodes.size(), "所有节点都应被路由到");
    }

    // ==================== 空环处理 ====================

    @Test
    public void testRouteNode_emptyRing() {
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(null, 10);
        assertNull(router.routeNode("anyKey"));
    }

    @Test
    public void testRouteNode_allNodesRemoved() {
        List<ServerNode> nodes = List.of(new ServerNode("192.168.1.1"));
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(nodes, 10);

        assertNotNull(router.routeNode("key"));

        router.removeNode(nodes.get(0));
        assertNull(router.routeNode("key"), "移除所有节点后应返回 null");
    }

    // ==================== 节点增删 ====================

    @Test
    public void testAddNode() {
        List<ServerNode> nodes = new ArrayList<>(List.of(
                new ServerNode("192.168.1.1"),
                new ServerNode("192.168.1.2")
        ));
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(nodes, 100);

        // 记录增加节点前各 key 的路由
        Map<String, String> beforeAdd = new HashMap<>();
        for (int i = 0; i < 500; i++) {
            String key = "key:" + i;
            beforeAdd.put(key, router.routeNode(key).getKey());
        }

        // 新增一个节点
        ServerNode newNode = new ServerNode("192.168.1.3");
        router.addNode(newNode, 100);

        // 统计路由变化的 key 数量
        int changed = 0;
        for (int i = 0; i < 500; i++) {
            String key = "key:" + i;
            String afterNode = router.routeNode(key).getKey();
            if (!beforeAdd.get(key).equals(afterNode)) {
                changed++;
            }
        }

        // 一致性哈希的优势：增加节点时，只有约 1/N 的 key 需要重新映射
        // 3 个节点时，理论约 33% 的 key 会变化
        double changeRate = (double) changed / 500;
        assertTrue(changeRate < 0.6, "增加节点后，变化的 key 比例应远小于 100%（实际: " +
                String.format("%.1f%%", changeRate * 100) + "）");
        assertTrue(changed > 0, "增加节点后应有一些 key 重新映射");

        System.out.println("增加 1 个节点后，" + changed + "/500 个 key 重新映射 (" +
                String.format("%.1f%%", changeRate * 100) + ")");
    }

    @Test
    public void testRemoveNode() {
        List<ServerNode> nodes = new ArrayList<>(List.of(
                new ServerNode("192.168.1.1"),
                new ServerNode("192.168.1.2"),
                new ServerNode("192.168.1.3")
        ));
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(nodes, 100);

        // 记录删除节点前各 key 的路由
        Map<String, String> beforeRemove = new HashMap<>();
        for (int i = 0; i < 500; i++) {
            String key = "key:" + i;
            beforeRemove.put(key, router.routeNode(key).getKey());
        }

        // 移除一个节点
        router.removeNode(nodes.get(2)); // 移除 192.168.1.3

        // 统计路由变化的 key 数量
        int changed = 0;
        int reMappedFromRemoved = 0;
        for (int i = 0; i < 500; i++) {
            String key = "key:" + i;
            String afterNode = router.routeNode(key).getKey();
            String beforeNode = beforeRemove.get(key);

            if (!beforeNode.equals(afterNode)) {
                changed++;
                if (beforeNode.equals("192.168.1.3")) {
                    reMappedFromRemoved++;
                }
            }
        }

        // 只有原本映射到被删除节点的 key 会变化
        double changeRate = (double) changed / 500;
        assertTrue(changeRate < 0.6, "删除节点后，变化的 key 比例应远小于 100%（实际: " +
                String.format("%.1f%%", changeRate * 100) + "）");

        System.out.println("删除 1 个节点后，" + changed + "/500 个 key 重新映射 (" +
                String.format("%.1f%%", changeRate * 100) + ")，其中 " + reMappedFromRemoved +
                " 个从被删除节点迁移");
    }

    // ==================== 虚拟节点与负载均衡 ====================

    @Test
    public void testVirtualNodeDistribution() {
        List<ServerNode> nodes = List.of(
                new ServerNode("server-A"),
                new ServerNode("server-B"),
                new ServerNode("server-C")
        );

        // 使用较多虚拟节点，分布应更均匀
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(nodes, 200);

        Map<String, Integer> counts = new HashMap<>();
        counts.put("server-A", 0);
        counts.put("server-B", 0);
        counts.put("server-C", 0);

        int total = 10000;
        for (int i = 0; i < total; i++) {
            ServerNode node = router.routeNode("key:" + i);
            counts.merge(node.getKey(), 1, Integer::sum);
        }

        // 3 个节点，理想均匀分布各 ~3333
        // 验证每个节点的分配比例在 20%~47% 之间（允许一定偏差）
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            double ratio = (double) entry.getValue() / total;
            assertTrue(ratio > 0.20 && ratio < 0.47,
                    "节点 " + entry.getKey() + " 分配比例 " +
                            String.format("%.1f%%", ratio * 100) + " 偏离过大");
            System.out.println("节点 " + entry.getKey() + ": " + entry.getValue() +
                    " 个 key (" + String.format("%.1f%%", ratio * 100) + ")");
        }
    }

    @Test
    public void testFewVirtualNodes_unbalancedDistribution() {
        List<ServerNode> nodes = List.of(
                new ServerNode("server-A"),
                new ServerNode("server-B"),
                new ServerNode("server-C")
        );

        // 虚拟节点数少时，分布会很不均匀
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(nodes, 1);

        Map<String, Integer> counts = new HashMap<>();
        counts.put("server-A", 0);
        counts.put("server-B", 0);
        counts.put("server-C", 0);

        int total = 10000;
        for (int i = 0; i < total; i++) {
            ServerNode node = router.routeNode("key:" + i);
            counts.merge(node.getKey(), 1, Integer::sum);
        }

        System.out.println("虚拟节点数 = 1 时的分布:");
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue() +
                    " (" + String.format("%.1f%%", (double) entry.getValue() / total * 100) + ")");
        }

        // 虚拟节点数太少时，至少验证能正常工作
        int sum = counts.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(total, sum, "所有 key 都应被路由");
    }

    // ==================== 已有副本数 ====================

    @Test
    public void testGetExistingReplicas() {
        List<ServerNode> nodes = List.of(new ServerNode("server-A"));
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(nodes, 50);

        int replicas = router.getExistingReplicas(nodes.get(0));
        assertEquals(50, replicas);
    }

    @Test
    public void testAddMoreReplicas() {
        ServerNode node = new ServerNode("server-A");
        List<ServerNode> nodes = List.of(node);
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(nodes, 30);

        assertEquals(30, router.getExistingReplicas(node));

        // 再增加 20 个虚拟节点
        router.addNode(node, 20);
        assertEquals(50, router.getExistingReplicas(node));
    }

    @Test
    public void testNegativeVirtualNodeCount() {
        ServerNode node = new ServerNode("server-A");
        ConsistentHashRouter<ServerNode> router = new ConsistentHashRouter<>(null, 0);

        assertThrows(IllegalArgumentException.class, () -> router.addNode(node, -1));
    }
}
