package org.hongxi.whatsmars.redis;

import org.hongxi.whatsmars.redis.sample.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Redis 示例入口
 *
 * 示例包含：
 * 1. 基本数据类型操作（String, List, Set, Hash, ZSet）
 * 2. 分布式锁实现（基于 Jedis SETNX + Lua 释放）
 * 3. 发布/订阅（Pub/Sub）
 * 4. Pipeline 批量操作
 * 5. 过期策略与淘汰策略演示
 * 6. Lua 脚本执行示例
 * 7. 位图（Bitmap）与 HyperLogLog 使用示例
 *
 * 启动参数（可选）：
 *   -Dredis.host=xxx -Dredis.port=6379 -Dredis.password=xxx
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(JedisConfig.class)) {
            log.info("========== 开始运行 Redis 示例 ==========\n");

            context.getBean(BasicDataTypeExample.class).runAll();
            context.getBean(DistributedLockExample.class).runAll();
            context.getBean(PubSubExample.class).runAll();
            context.getBean(PipelineExample.class).runAll();
            context.getBean(ExpirationExample.class).runAll();
            context.getBean(LuaScriptExample.class).runAll();
            context.getBean(BitmapAndHyperLogLogExample.class).runAll();

            log.info("========== 所有 Redis 示例运行完毕 ==========");
        } catch (Exception e) {
            log.error("运行失败", e);
        }
    }
}
