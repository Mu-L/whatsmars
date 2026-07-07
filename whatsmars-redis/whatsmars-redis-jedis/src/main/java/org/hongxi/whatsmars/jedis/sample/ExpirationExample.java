package org.hongxi.whatsmars.jedis.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 过期策略与淘汰策略演示
 *
 * Redis 过期相关命令：
 * - EXPIRE key seconds    设置过期时间（秒）
 * - PEXPIRE key ms        设置过期时间（毫秒）
 * - EXPIREAT key timestamp 设置到指定时间戳过期
 * - TTL key               获取剩余过期时间（秒）
 * - PTTL key              获取剩余过期时间（毫秒）
 * - PERSIST key           移除过期时间（永久保留）
 *
 * Redis 淘汰策略（maxmemory-policy）：
 * - noeviction          不淘汰，内存满时返回错误（默认）
 * - allkeys-lru         淘汰最近最少使用的 key（最常用）
 * - volatile-lru        淘汰设置了过期时间的 key 中最近最少使用的
 * - allkeys-lfu         淘汰最不常用的 key（Redis 4.0+）
 * - volatile-lfu        淘汰设置了过期时间的 key 中最不常用的
 * - allkeys-random      随机淘汰
 * - volatile-random     在设置了过期时间的 key 中随机淘汰
 * - volatile-ttl        淘汰 TTL 最短的 key
 */
@Component
public class ExpirationExample {

    private static final Logger log = LoggerFactory.getLogger(ExpirationExample.class);

    private final JedisPool jedisPool;

    public ExpirationExample(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void runAll() {
        log.info("========== 过期策略与淘汰策略示例 ==========");
        expireDemo();
        expireAtDemo();
        persistDemo();
        evictionPolicyDemo();
        log.info("========== 过期策略示例结束 ==========\n");
    }

    // ==================== EXPIRE / TTL ====================

    /**
     * 设置过期时间并观察 TTL 变化
     */
    private void expireDemo() {
        log.info("--- EXPIRE / TTL 演示 ---");

        try (Jedis jedis = jedisPool.getResource()) {
            // SETEX：设置值并指定过期时间（秒）
            jedis.setex("exp:session", 30, "session-data-xyz");
            log.info("  SETEX exp:session 30s -> TTL = {}s", jedis.ttl("exp:session"));

            // EXPIRE：给已有 key 设置过期时间
            jedis.set("exp:temp", "temporary-data");
            log.info("  SET exp:temp -> TTL = {} (-1=永不过期)", jedis.ttl("exp:temp"));
            jedis.expire("exp:temp", 10);
            log.info("  EXPIRE 10s -> TTL = {}s", jedis.ttl("exp:temp"));

            // PEXPIRE：毫秒级过期
            jedis.set("exp:ms", "millisecond-data");
            jedis.pexpire("exp:ms", 5000);
            log.info("  PEXPIRE 5000ms -> PTTL = {}ms", jedis.pttl("exp:ms"));

            // 观察 key 过期后不存在
            jedis.setex("exp:short", 1, "will-disappear");
            log.info("  GET exp:short (1s TTL) = {}", jedis.get("exp:short"));
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("  GET exp:short (已过期) = {}", jedis.get("exp:short"));

            jedis.del("exp:session", "exp:temp", "exp:ms");
        }
    }

    // ==================== EXPIREAT ====================

    /**
     * 设置到指定时间戳过期
     */
    private void expireAtDemo() {
        log.info("--- EXPIREAT 演示 ---");

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("exp:deadline", "deadline-data");

            // 设置到 30 秒后的时间戳过期
            long futureTimestamp = System.currentTimeMillis() / 1000 + 30;
            jedis.expireAt("exp:deadline", futureTimestamp);
            log.info("  EXPIREAT 30秒后 -> TTL = {}s", jedis.ttl("exp:deadline"));

            // pexpireAt：毫秒时间戳
            jedis.set("exp:ms-deadline", "ms-deadline-data");
            long futureMs = System.currentTimeMillis() + 10000;
            jedis.pexpireAt("exp:ms-deadline", futureMs);
            log.info("  PEXPIREAT 10秒后 -> PTTL = {}ms", jedis.pttl("exp:ms-deadline"));

            jedis.del("exp:deadline", "exp:ms-deadline");
        }
    }

    // ==================== PERSIST ====================

    /**
     * 移除过期时间，使 key 永久保留
     */
    private void persistDemo() {
        log.info("--- PERSIST 演示 ---");

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex("exp:persist", 60, "persistent-data");
            log.info("  SETEX 60s -> TTL = {}s", jedis.ttl("exp:persist"));

            // PERSIST：移除过期时间
            long persistResult = jedis.persist("exp:persist");
            log.info("  PERSIST -> 结果: {} (1=成功), TTL = {} (-1=永不过期)",
                    persistResult, jedis.ttl("exp:persist"));

            jedis.del("exp:persist");
        }
    }

    // ==================== 淘汰策略查询 ====================

    /**
     * 查看当前 Redis 的内存配置和淘汰策略
     */
    private void evictionPolicyDemo() {
        log.info("--- 淘汰策略信息 ---");

        try (Jedis jedis = jedisPool.getResource()) {
            // 查看 maxmemory 配置
            String maxMemory = jedis.configGet("maxmemory").getOrDefault("maxmemory", "unknown");
            log.info("  maxmemory = {} (0=无限制)", maxMemory);

            // 查看当前淘汰策略
            String policy = jedis.configGet("maxmemory-policy").getOrDefault("maxmemory-policy", "unknown");
            log.info("  当前淘汰策略 = {}", policy);

            // 查看内存使用
            String usedMemory = jedis.info("memory");
            // 解析 used_memory_human
            for (String line : usedMemory.split("\n")) {
                if (line.startsWith("used_memory_human:")) {
                    log.info("  已用内存 = {}", line.split(":")[1].trim());
                }
                if (line.startsWith("maxmemory_human:") && !line.contains("0B")) {
                    log.info("  最大内存 = {}", line.split(":")[1].trim());
                }
                if (line.startsWith("evicted_keys:")) {
                    log.info("  已淘汰 key 数 = {}", line.split(":")[1].trim());
                }
            }

            log.info("  常见淘汰策略说明：");
            log.info("    noeviction    - 内存满时返回错误（默认）");
            log.info("    allkeys-lru   - 淘汰最近最少使用的 key（推荐，适用于缓存）");
            log.info("    volatile-lru  - 仅淘汰有过期时间的 key 中最近最少使用的");
            log.info("    allkeys-lfu   - 淘汰最不常用的 key（Redis 4.0+，推荐）");
            log.info("    volatile-ttl  - 淘汰 TTL 最短的 key");
        }
    }
}
