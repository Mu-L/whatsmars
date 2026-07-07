package org.hongxi.whatsmars.jedis.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;

/**
 * 基本数据类型操作示例：String、List、Set、Hash、ZSet
 */
@Component
public class BasicDataTypeExample {

    private static final Logger log = LoggerFactory.getLogger(BasicDataTypeExample.class);

    private final JedisPool jedisPool;

    public BasicDataTypeExample(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void runAll() {
        log.info("========== 基本数据类型示例 ==========");
        stringOps();
        listOps();
        setOps();
        hashOps();
        zsetOps();
        log.info("========== 基本数据类型示例结束 ==========\n");
    }

    // ==================== String ====================

    /**
     * String 类型：set/get/mset/mget/incr/append
     */
    private void stringOps() {
        log.info("--- String 类型操作 ---");
        try (Jedis jedis = jedisPool.getResource()) {
            // 基础 set/get
            jedis.set("str:name", "whatsmars");
            log.info("  GET str:name = {}", jedis.get("str:name"));

            // setex：设置值并指定过期时间（秒）
            jedis.setex("str:session", 60, "token-abc-123");
            log.info("  GET str:session (60s TTL) = {}, TTL = {}s",
                    jedis.get("str:session"), jedis.ttl("str:session"));

            // mset/mget：批量设置/获取
            jedis.mset("str:k1", "v1", "str:k2", "v2", "str:k3", "v3");
            List<String> values = jedis.mget("str:k1", "str:k2", "str:k3");
            log.info("  MGET k1,k2,k3 = {}", values);

            // incr/decr：原子自增/自减
            jedis.set("str:counter", "100");
            jedis.incr("str:counter");
            jedis.incrBy("str:counter", 10);
            log.info("  counter (100 -> incr -> incrBy 10) = {}", jedis.get("str:counter"));

            // append：追加字符串
            jedis.set("str:greeting", "Hello");
            jedis.append("str:greeting", " World");
            log.info("  GET str:greeting = {}", jedis.get("str:greeting"));

            // setnx：仅在 key 不存在时设置
            long setnxResult = jedis.setnx("str:name", "other-value");
            log.info("  SETNX str:name（已存在）= {}（0=未设置）", setnxResult);

            // 清理
            jedis.del("str:name", "str:session", "str:k1", "str:k2", "str:k3", "str:counter", "str:greeting");
        }
    }

    // ==================== List ====================

    /**
     * List 类型：lpush/rpush/lpop/rpop/lrange/llen
     */
    private void listOps() {
        log.info("--- List 类型操作 ---");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("list:queue");

            // rpush：从右端推入（队列尾部）
            jedis.rpush("list:queue", "task1", "task2", "task3");
            log.info("  RPUSH task1,task2,task3 -> LRANGE = {}", jedis.lrange("list:queue", 0, -1));

            // lpush：从左端推入
            jedis.lpush("list:queue", "task0");
            log.info("  LPUSH task0 -> LRANGE = {}", jedis.lrange("list:queue", 0, -1));

            // lpop/rpop：从左/右端弹出
            log.info("  LPOP = {}", jedis.lpop("list:queue"));
            log.info("  RPOP = {}", jedis.rpop("list:queue"));

            // llen：列表长度
            log.info("  LLEN = {}", jedis.llen("list:queue"));

            // lindex：按索引取值
            log.info("  LINDEX 0 = {}", jedis.lindex("list:queue", 0));

            // 阻塞弹出（演示 blpop，超时 1 秒）
            jedis.rpush("list:blocking", "item1");
            List<String> blpopResult = jedis.blpop(1, "list:blocking");
            log.info("  BLPOP list:blocking = {}", blpopResult);

            jedis.del("list:queue", "list:blocking");
        }
    }

    // ==================== Set ====================

    /**
     * Set 类型：sadd/smembers/sismember/sinter/sunion/sdiff
     */
    private void setOps() {
        log.info("--- Set 类型操作 ---");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("set:a", "set:b");

            // sadd：添加成员
            jedis.sadd("set:a", "apple", "banana", "cherry", "date");
            jedis.sadd("set:b", "banana", "cherry", "elderberry", "fig");
            log.info("  SMEMBERS set:a = {}", jedis.smembers("set:a"));
            log.info("  SMEMBERS set:b = {}", jedis.smembers("set:b"));

            // sismember：判断成员是否存在
            log.info("  SISMEMBER set:a 'apple' = {}", jedis.sismember("set:a", "apple"));
            log.info("  SISMEMBER set:a 'fig' = {}", jedis.sismember("set:a", "fig"));

            // sinter：交集
            log.info("  SINTER set:a ∩ set:b = {}", jedis.sinter("set:a", "set:b"));

            // sunion：并集
            log.info("  SUNION set:a ∪ set:b = {}", jedis.sunion("set:a", "set:b"));

            // sdiff：差集（a - b）
            log.info("  SDIFF set:a - set:b = {}", jedis.sdiff("set:a", "set:b"));

            // scard：成员数量
            log.info("  SCARD set:a = {}", jedis.scard("set:a"));

            jedis.del("set:a", "set:b");
        }
    }

    // ==================== Hash ====================

    /**
     * Hash 类型：hset/hget/hmset/hgetAll/hincrBy/hdel
     */
    private void hashOps() {
        log.info("--- Hash 类型操作 ---");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("hash:user");

            // hset：设置单个字段
            jedis.hset("hash:user", "name", "张三");
            jedis.hset("hash:user", "age", "28");
            log.info("  HGET hash:user name = {}", jedis.hget("hash:user", "name"));

            // hmset：批量设置字段
            jedis.hmset("hash:user", Map.of(
                    "email", "zhangsan@example.com",
                    "city", "北京",
                    "role", "developer"
            ));

            // hgetAll：获取所有字段和值
            log.info("  HGETALL hash:user = {}", jedis.hgetAll("hash:user"));

            // hincrBy：字段原子自增
            jedis.hincrBy("hash:user", "age", 2);
            log.info("  HINCRBY age += 2 -> age = {}", jedis.hget("hash:user", "age"));

            // hexists：判断字段是否存在
            log.info("  HEXISTS email = {}", jedis.hexists("hash:user", "email"));

            // hdel：删除字段
            jedis.hdel("hash:user", "role");
            log.info("  HDEL role -> HGETALL = {}", jedis.hgetAll("hash:user"));

            // hkeys/hvals
            log.info("  HKEYS = {}", jedis.hkeys("hash:user"));
            log.info("  HVALS = {}", jedis.hvals("hash:user"));

            jedis.del("hash:user");
        }
    }

    // ==================== ZSet（有序集合）====================

    /**
     * ZSet 类型：zadd/zrange/zrangeByScore/zrank/zincrby/zrem
     */
    private void zsetOps() {
        log.info("--- ZSet 有序集合操作 ---");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("zset:scores");

            // zadd：添加成员及分数
            jedis.zadd("zset:scores", 85.5, "Alice");
            jedis.zadd("zset:scores", 92.0, "Bob");
            jedis.zadd("zset:scores", 78.0, "Charlie");
            jedis.zadd("zset:scores", 95.5, "Diana");
            jedis.zadd("zset:scores", 88.0, "Eve");

            // zrange：按分数从低到高
            log.info("  ZRANGE 0,-1 (低->高) = {}", jedis.zrange("zset:scores", 0, -1));

            // zrevrange：按分数从高到低
            log.info("  ZREVRANGE 0,-1 (高->低) = {}", jedis.zrevrange("zset:scores", 0, -1));

            // zrangeByScore：按分数区间查询
            log.info("  ZRANGEBYSCORE 80~95 = {}", jedis.zrangeByScore("zset:scores", 80, 95));

            // zrank：排名（从 0 开始，低分排前面）
            log.info("  ZRANK Bob = {}", jedis.zrank("zset:scores", "Bob"));

            // zrevrank：逆序排名（高分排前面）
            log.info("  ZREVRANK Diana = {}", jedis.zrevrank("zset:scores", "Diana"));

            // zincrby：增加分数
            jedis.zincrby("zset:scores", 10.0, "Charlie");
            log.info("  ZINCRBY Charlie +10 -> score = {}", jedis.zscore("zset:scores", "Charlie"));

            // zcard：成员数量
            log.info("  ZCARD = {}", jedis.zcard("zset:scores"));

            // zcount：分数区间内的成员数
            log.info("  ZCOUNT 80~100 = {}", jedis.zcount("zset:scores", 80, 100));

            jedis.del("zset:scores");
        }
    }
}
