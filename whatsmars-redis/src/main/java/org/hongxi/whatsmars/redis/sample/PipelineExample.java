package org.hongxi.whatsmars.redis.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.List;

/**
 * Pipeline 批量操作示例
 *
 * 核心优势：
 * - 普通模式：每条命令都要一次网络往返（RTT），1000 条命令 = 1000 次 RTT
 * - Pipeline：将多条命令打包一次性发送，1000 条命令 = 1 次 RTT
 * - 性能可提升 10~50 倍（取决于网络延迟）
 *
 * 注意事项：
 * - Pipeline 不保证原子性（如果中间某条失败，其他命令仍会执行）
 * - 如需原子性，请使用 Lua 脚本或 MULTI/EXEC 事务
 * - Pipeline 中命令不宜过多（建议 500~1000 条），否则占用客户端内存过大
 */
@Component
public class PipelineExample {

    private static final Logger log = LoggerFactory.getLogger(PipelineExample.class);

    private final JedisPool jedisPool;

    public PipelineExample(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void runAll() {
        log.info("========== Pipeline 批量操作示例 ==========");
        basicPipelineDemo();
        pipelineWithResponseDemo();
        pipelinePerformanceDemo();
        log.info("========== Pipeline 示例结束 ==========\n");
    }

    // ==================== 基础 Pipeline ====================

    /**
     * 基础 Pipeline：批量 SET/GET
     */
    private void basicPipelineDemo() {
        log.info("--- 基础 Pipeline ---");

        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipeline = jedis.pipelined();

            // 批量写入 10 个 key
            for (int i = 0; i < 10; i++) {
                pipeline.set("pipe:key:" + i, "value-" + i);
            }

            // 一次性发送所有命令并获取结果
            List<Object> results = pipeline.syncAndReturnAll();
            log.info("  批量 SET 10 个 key，结果数: {}", results.size());

            // 验证：批量读取
            Pipeline readPipeline = jedis.pipelined();
            for (int i = 0; i < 10; i++) {
                readPipeline.get("pipe:key:" + i);
            }
            List<Object> values = readPipeline.syncAndReturnAll();
            log.info("  批量 GET 结果: {}", values);

            // 清理
            Pipeline delPipeline = jedis.pipelined();
            for (int i = 0; i < 10; i++) {
                delPipeline.del("pipe:key:" + i);
            }
            delPipeline.sync();
        }
    }

    // ==================== Pipeline + Response 获取中间结果 ====================

    /**
     * 使用 Response 对象在 sync 之前引用中间结果
     */
    private void pipelineWithResponseDemo() {
        log.info("--- Pipeline + Response ---");

        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipeline = jedis.pipelined();

            // 写入计数器并执行多次自增
            pipeline.set("pipe:counter", "0");
            Response<Long> r1 = pipeline.incr("pipe:counter");  // 0 -> 1
            Response<Long> r2 = pipeline.incr("pipe:counter");  // 1 -> 2
            Response<Long> r3 = pipeline.incrBy("pipe:counter", 10); // 2 -> 12
            Response<String> finalVal = pipeline.get("pipe:counter");

            // sync 之前不能调用 r1.get()，会抛异常
            pipeline.sync();

            // sync 之后可以获取所有 Response 的值
            log.info("  INCR -> {}, INCR -> {}, INCRBY 10 -> {}, 最终值: {}",
                    r1.get(), r2.get(), r3.get(), finalVal.get());

            jedis.del("pipe:counter");
        }
    }

    // ==================== Pipeline 性能对比 ====================

    /**
     * 对比普通模式与 Pipeline 模式的性能差异
     */
    private void pipelinePerformanceDemo() {
        log.info("--- Pipeline 性能对比 ---");

        int count = 1000;

        // 1. 普通模式：逐条执行
        long start1 = System.currentTimeMillis();
        try (Jedis jedis = jedisPool.getResource()) {
            for (int i = 0; i < count; i++) {
                jedis.set("perf:normal:" + i, "value");
            }
            // 清理
            for (int i = 0; i < count; i++) {
                jedis.del("perf:normal:" + i);
            }
        }
        long normalTime = System.currentTimeMillis() - start1;

        // 2. Pipeline 模式：批量执行
        long start2 = System.currentTimeMillis();
        try (Jedis jedis = jedisPool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            for (int i = 0; i < count; i++) {
                pipeline.set("perf:pipe:" + i, "value");
            }
            pipeline.sync();

            // 清理
            Pipeline delPipeline = jedis.pipelined();
            for (int i = 0; i < count; i++) {
                delPipeline.del("perf:pipe:" + i);
            }
            delPipeline.sync();
        }
        long pipelineTime = System.currentTimeMillis() - start2;

        log.info("  {} 条命令 - 普通模式: {}ms, Pipeline: {}ms",
                count, normalTime, pipelineTime);
        if (pipelineTime > 0) {
            log.info("  Pipeline 提速约 {} 倍", normalTime / pipelineTime);
        }
    }
}
