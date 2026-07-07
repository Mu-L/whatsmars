package org.hongxi.whatsmars.jedis.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.List;

/**
 * Lua 脚本执行示例
 *
 * 核心优势：
 * - 原子性：整个 Lua 脚本作为原子操作执行，不会被其他命令插入
 * - 减少网络开销：多个命令合并为一次请求
 * - 可复用：EVALSHA 可以缓存脚本，避免重复传输
 *
 * 使用方式：
 * - EVAL script numkeys keys args     执行 Lua 脚本
 * - EVALSHA sha1 numkeys keys args    执行已缓存的脚本
 * - SCRIPT LOAD script                加载脚本到缓存（返回 SHA1）
 * - SCRIPT EXISTS sha1                检查脚本是否在缓存中
 */
@Component
public class LuaScriptExample {

    private static final Logger log = LoggerFactory.getLogger(LuaScriptExample.class);

    private final JedisPool jedisPool;

    public LuaScriptExample(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void runAll() {
        log.info("========== Lua 脚本示例 ==========");
        atomicIncrDemo();
        casDemo();
        rateLimiterDemo();
        evalShaDemo();
        log.info("========== Lua 脚本示例结束 ==========\n");
    }

    // ==================== 原子自增（带上限）====================

    /**
     * 带上限的原子自增：如果自增后超过 limit 则不执行，返回 -1
     */
    private void atomicIncrDemo() {
        log.info("--- 带上限的原子自增 ---");

        String luaScript = """
                local current = tonumber(redis.call("get", KEYS[1]) or "0")
                local limit = tonumber(ARGV[1])
                local increment = tonumber(ARGV[2])
                if current + increment > limit then
                    return -1
                else
                    return redis.call("incrby", KEYS[1], increment)
                end
                """;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("lua:stock", "8");

            // 库存 8，尝试扣减 3（成功，剩余 5）
            Object r1 = jedis.eval(luaScript,
                    Collections.singletonList("lua:stock"),
                    List.of("10", "3"));
            log.info("  库存8, 扣减3 (上限10) -> 结果: {} (-1=超限)", r1);

            // 库存 5，尝试扣减 8（超过上限 10，失败）
            Object r2 = jedis.eval(luaScript,
                    Collections.singletonList("lua:stock"),
                    List.of("10", "8"));
            log.info("  当前值, 扣减8 (上限10) -> 结果: {} (-1=超限)", r2);

            log.info("  最终库存: {}", jedis.get("lua:stock"));
            jedis.del("lua:stock");
        }
    }

    // ==================== Compare-And-Set（CAS）====================

    /**
     * CAS 操作：只有当前值等于 expect 时才更新为 newValue
     */
    private void casDemo() {
        log.info("--- CAS (Compare-And-Set) ---");

        String luaScript = """
                local current = redis.call("get", KEYS[1])
                if current == ARGV[1] then
                    redis.call("set", KEYS[1], ARGV[2])
                    return 1
                else
                    return 0
                end
                """;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("lua:cas", "old-value");

            // CAS: old-value -> new-value（应成功）
            Object r1 = jedis.eval(luaScript,
                    Collections.singletonList("lua:cas"),
                    List.of("old-value", "new-value"));
            log.info("  CAS old-value -> new-value = {} (1=成功)", r1);

            // CAS: old-value -> other（应失败，当前值已变）
            Object r2 = jedis.eval(luaScript,
                    Collections.singletonList("lua:cas"),
                    List.of("old-value", "other"));
            log.info("  CAS old-value -> other = {} (0=失败)", r2);

            log.info("  最终值: {}", jedis.get("lua:cas"));
            jedis.del("lua:cas");
        }
    }

    // ==================== 限流器 ====================

    /**
     * 基于滑动窗口的限流器（简化版）
     * 限制每个用户在 timeWindow 秒内最多 maxRequests 次请求
     */
    private void rateLimiterDemo() {
        log.info("--- Lua 限流器 ---");

        String luaScript = """
                local key = KEYS[1]
                local max = tonumber(ARGV[1])
                local window = tonumber(ARGV[2])
                local current = tonumber(redis.call("get", key) or "0")
                if current >= max then
                    return 0
                else
                    redis.call("incr", key)
                    if current == 0 then
                        redis.call("expire", key, window)
                    end
                    return 1
                end
                """;

        try (Jedis jedis = jedisPool.getResource()) {
            String key = "lua:ratelimit:user:1001";
            jedis.del(key);

            int maxRequests = 5;
            int windowSeconds = 10;

            log.info("  限流策略: {}秒内最多{}次请求", windowSeconds, maxRequests);

            for (int i = 1; i <= 7; i++) {
                Object result = jedis.eval(luaScript,
                        Collections.singletonList(key),
                        List.of(String.valueOf(maxRequests), String.valueOf(windowSeconds)));
                String status = Long.valueOf(1).equals(result) ? "通过" : "拒绝";
                log.info("  第{}次请求 -> {}", i, status);
            }

            jedis.del(key);
        }
    }

    // ==================== EVALSHA 脚本缓存 ====================

    /**
     * 使用 SCRIPT LOAD + EVALSHA 复用已缓存的脚本，减少网络传输
     */
    private void evalShaDemo() {
        log.info("--- EVALSHA 脚本缓存 ---");

        String luaScript = """
                return redis.call("set", KEYS[1], ARGV[1] .. "-" .. ARGV[2])
                """;

        try (Jedis jedis = jedisPool.getResource()) {
            // 1. SCRIPT LOAD：加载脚本到 Redis 缓存，返回 SHA1
            String sha1 = jedis.scriptLoad(luaScript);
            log.info("  SCRIPT LOAD -> SHA1: {}", sha1);

            // 2. SCRIPT EXISTS：检查脚本是否在缓存中
            Boolean exists = jedis.scriptExists(sha1);
            log.info("  SCRIPT EXISTS = {}", exists);

            // 3. EVALSHA：通过 SHA1 执行缓存的脚本（不需要再传输完整脚本）
            Object result = jedis.evalsha(sha1,
                    Collections.singletonList("lua:cached"),
                    List.of("hello", "world"));
            log.info("  EVALSHA 执行结果: {}", result);
            log.info("  GET lua:cached = {}", jedis.get("lua:cached"));

            jedis.del("lua:cached");
        }
    }
}
