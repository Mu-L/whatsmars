package org.hongxi.whatsmars.redis.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;

/**
 * 分布式锁实现示例（基于 Jedis）
 *
 * 核心思路：
 * - 加锁：SET key value NX PX timeout（原子操作，设置值+过期时间）
 * - 解锁：Lua 脚本保证 "判断值 + 删除" 的原子性，防止误删其他线程的锁
 * - 看门狗（可选）：锁快过期时自动续期
 */
@Component
public class DistributedLockExample {

    private static final Logger log = LoggerFactory.getLogger(DistributedLockExample.class);

    private final JedisPool jedisPool;

    public DistributedLockExample(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void runAll() {
        log.info("========== 分布式锁示例 ==========");
        basicLockDemo();
        reentrantLockDemo();
        log.info("========== 分布式锁示例结束 ==========\n");
    }

    // ==================== 基础分布式锁 ====================

    /**
     * 基础分布式锁演示：加锁 → 执行操作 → 释放锁
     */
    private void basicLockDemo() {
        log.info("--- 基础分布式锁 ---");

        String lockKey = "lock:basic";
        String requestId = UUID.randomUUID().toString();

        // 1. 尝试加锁
        boolean locked = tryLock(lockKey, requestId, 5000);
        log.info("  加锁结果: {}", locked);

        if (locked) {
            try {
                // 2. 执行业务逻辑（模拟耗时操作）
                log.info("  获取锁成功，执行业务逻辑...");
                Thread.sleep(200);
                log.info("  业务逻辑执行完毕");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // 3. 释放锁
                boolean released = releaseLock(lockKey, requestId);
                log.info("  释放锁结果: {}", released);
            }
        }

        // 4. 演示锁竞争：第二次加锁（不同 requestId，锁已被释放，应成功）
        String requestId2 = UUID.randomUUID().toString();
        boolean locked2 = tryLock(lockKey, requestId2, 5000);
        log.info("  第二次加锁（锁已释放）: {}", locked2);
        releaseLock(lockKey, requestId2);
    }

    // ==================== 模拟重入/续期 ====================

    /**
     * 模拟看门狗续期机制：锁快到期时自动延长过期时间
     */
    private void reentrantLockDemo() {
        log.info("--- 锁续期（看门狗）演示 ---");

        String lockKey = "lock:watchdog";
        String requestId = UUID.randomUUID().toString();

        boolean locked = tryLock(lockKey, requestId, 3000); // 3秒过期
        log.info("  加锁（3s TTL）: {}", locked);

        if (locked) {
            try {
                // 模拟长时间任务：每 1 秒续期一次，共执行 5 秒
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(1000);
                    long ttl = getTtl(lockKey);
                    log.info("  第{}秒, 剩余TTL={}ms", i, ttl);

                    if (ttl > 0 && ttl < 2000) {
                        // 看门狗：剩余时间不足 2 秒，续期到 3 秒
                        renewLock(lockKey, requestId, 3000);
                        log.info("  >>> 看门狗续期，新TTL={}ms", getTtl(lockKey));
                    }
                }
                log.info("  长任务完成");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                releaseLock(lockKey, requestId);
                log.info("  锁已释放");
            }
        }
    }

    // ==================== 锁工具方法 ====================

    /**
     * 尝试加锁
     * SET key value NX PX milliseconds
     *
     * @param lockKey        锁的 key
     * @param requestId      请求标识（用于安全释放，通常为 UUID）
     * @param expireMillis   锁的过期时间（毫秒）
     * @return 是否加锁成功
     */
    private boolean tryLock(String lockKey, String requestId, long expireMillis) {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.set(lockKey, requestId, SetParams.setParams().nx().px(expireMillis));
            return "OK".equals(result);
        }
    }

    /**
     * 释放锁（Lua 脚本保证原子性）
     *
     * 只有 requestId 匹配时才删除，防止误删其他线程持有的锁。
     * 如果不用 Lua，"GET + 比较 + DEL" 三步非原子，在高并发下可能出问题。
     */
    private boolean releaseLock(String lockKey, String requestId) {
        String luaScript = """
                if redis.call("get", KEYS[1]) == ARGV[1] then
                    return redis.call("del", KEYS[1])
                else
                    return 0
                end
                """;
        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.eval(luaScript,
                    Collections.singletonList(lockKey),
                    Collections.singletonList(requestId));
            return Long.valueOf(1).equals(result);
        }
    }

    /**
     * 锁续期（看门狗）
     * 只有 requestId 匹配时才延长过期时间
     */
    private boolean renewLock(String lockKey, String requestId, long expireMillis) {
        String luaScript = """
                if redis.call("get", KEYS[1]) == ARGV[1] then
                    return redis.call("pexpire", KEYS[1], ARGV[2])
                else
                    return 0
                end
                """;
        try (Jedis jedis = jedisPool.getResource()) {
            Object result = jedis.eval(luaScript,
                    Collections.singletonList(lockKey),
                    java.util.List.of(requestId, String.valueOf(expireMillis)));
            return Long.valueOf(1).equals(result);
        }
    }

    /**
     * 获取锁的剩余 TTL（毫秒）
     */
    private long getTtl(String lockKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pttl(lockKey);
        }
    }
}
