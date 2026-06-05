package org.hongxi.whatsmars.common.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LRUCache 缓存淘汰策略单元测试
 */
public class LRUCacheTest {

    // ==================== 基本操作 ====================

    @Test
    public void testPutAndGet() {
        LRUCache<String, String> cache = new LRUCache<>(10);
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        assertEquals(2, cache.size());
    }

    @Test
    public void testGetNonExistent() {
        LRUCache<String, String> cache = new LRUCache<>(10);
        assertNull(cache.get("nonexistent"));
    }

    @Test
    public void testContainsKey() {
        LRUCache<String, String> cache = new LRUCache<>(10);
        cache.put("key1", "value1");

        assertTrue(cache.containsKey("key1"));
        assertFalse(cache.containsKey("key2"));
    }

    @Test
    public void testRemove() {
        LRUCache<String, String> cache = new LRUCache<>(10);
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        String removed = cache.remove("key1");
        assertEquals("value1", removed);
        assertNull(cache.get("key1"));
        assertEquals(1, cache.size());
    }

    @Test
    public void testClear() {
        LRUCache<String, String> cache = new LRUCache<>(10);
        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");

        cache.clear();
        assertEquals(0, cache.size());
        assertNull(cache.get("a"));
    }

    // ==================== LRU 淘汰策略 ====================

    @Test
    public void testEviction_exceedCapacity() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);

        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");
        assertEquals(3, cache.size());

        // 插入第 4 个元素，key=1 应被淘汰（最久未访问）
        cache.put(4, "d");
        assertEquals(3, cache.size());
        assertNull(cache.get(1), "key=1 应被淘汰");
        assertEquals("b", cache.get(2));
        assertEquals("c", cache.get(3));
        assertEquals("d", cache.get(4));
    }

    @Test
    public void testEviction_accessRefreshesOrder() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);

        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");

        // 访问 key=1，使其变为最近使用
        cache.get(1);

        // 插入第 4 个元素，key=2 应被淘汰（key=1 刚被访问过）
        cache.put(4, "d");
        assertEquals(3, cache.size());
        assertEquals("a", cache.get(1), "key=1 刚被访问，不应被淘汰");
        assertNull(cache.get(2), "key=2 应被淘汰");
        assertEquals("c", cache.get(3));
        assertEquals("d", cache.get(4));
    }

    @Test
    public void testEviction_putExistingKeyRefreshesOrder() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);

        cache.put(1, "a");
        cache.put(2, "b");
        cache.put(3, "c");

        // 更新 key=1 的值，使其变为最近使用
        cache.put(1, "a-updated");

        // 插入第 4 个元素，key=2 应被淘汰
        cache.put(4, "d");
        assertEquals("a-updated", cache.get(1), "key=1 刚被更新，不应被淘汰");
        assertNull(cache.get(2), "key=2 应被淘汰");
    }

    @Test
    public void testEviction_capacityOne() {
        LRUCache<String, String> cache = new LRUCache<>(1);

        cache.put("a", "1");
        assertEquals("1", cache.get("a"));

        cache.put("b", "2");
        assertNull(cache.get("a"), "容量为 1 时，插入新元素应淘汰旧元素");
        assertEquals("2", cache.get("b"));
    }

    @Test
    public void testEviction_multipleEvictions() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);

        for (int i = 1; i <= 10; i++) {
            cache.put(i, "v" + i);
        }

        // 只有最后 3 个元素应存活
        assertEquals(3, cache.size());
        assertNull(cache.get(7));
        assertEquals("v8", cache.get(8));
        assertEquals("v9", cache.get(9));
        assertEquals("v10", cache.get(10));
    }

    // ==================== 容量配置 ====================

    @Test
    public void testDefaultCapacity() {
        LRUCache<String, String> cache = new LRUCache<>();
        assertEquals(1000, cache.getMaxCapacity());
    }

    @Test
    public void testSetMaxCapacity() {
        LRUCache<String, String> cache = new LRUCache<>(10);
        assertEquals(10, cache.getMaxCapacity());

        cache.setMaxCapacity(20);
        assertEquals(20, cache.getMaxCapacity());
    }

    /**
     * 验证动态调整容量后的淘汰行为。
     * 注意：LinkedHashMap.removeEldestEntry() 每次 put 只淘汰一个最旧条目，不会批量淘汰，
     * 因此缩小容量后 size 不会立即降到新容量，而是每次 put 递减一个。
     */
    @Test
    public void testReduceMaxCapacityTriggersEviction() {
        LRUCache<Integer, String> cache = new LRUCache<>(3);
        cache.put(1, "v1");
        cache.put(2, "v2");
        cache.put(3, "v3");
        assertEquals(3, cache.size());

        // 扩大容量，新元素正常插入
        cache.setMaxCapacity(5);
        cache.put(4, "v4");
        cache.put(5, "v5");
        assertEquals(5, cache.size());
        assertEquals("v1", cache.get(1), "容量扩大后旧元素应仍存在");

        // 缩小容量到 3，每次 put 只淘汰一个最旧条目
        cache.setMaxCapacity(3);
        cache.put(6, "v6"); // 插入后 size=6, removeEldestEntry 淘汰1个 → size=5
        assertEquals(5, cache.size());
        assertNull(cache.get(2), "最旧的 key=2 应已被淘汰");
        assertEquals("v6", cache.get(6));

        // 持续插入新元素，每次淘汰一个
        cache.put(7, "v7");
        cache.put(8, "v8");
        assertEquals("v8", cache.get(8));
        assertTrue(cache.size() <= 5, "size 不应无限增长");
    }

    // ==================== 并发安全 ====================

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        LRUCache<Integer, String> cache = new LRUCache<>(100);
        int threadCount = 10;
        int opsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        int key = threadId * opsPerThread + i;
                        cache.put(key, "v" + key);
                        cache.get(key);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // 验证不会因并发导致异常或数据不一致
        assertTrue(cache.size() <= 100, "缓存大小不应超过最大容量");
        assertTrue(cache.size() > 0, "缓存不应为空");
    }
}
