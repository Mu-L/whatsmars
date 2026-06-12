package org.hongxi.whatsmars.redis;

import org.hongxi.whatsmars.redis.sample.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
 */
@SpringBootApplication
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private BasicDataTypeExample basicDataTypeExample;
    @Autowired
    private DistributedLockExample distributedLockExample;
    @Autowired
    private PubSubExample pubSubExample;
    @Autowired
    private PipelineExample pipelineExample;
    @Autowired
    private ExpirationExample expirationExample;
    @Autowired
    private LuaScriptExample luaScriptExample;
    @Autowired
    private BitmapAndHyperLogLogExample bitmapAndHyperLogLogExample;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner runner() {
        return args -> {
            try {
                log.info("========== 运行 Redis 示例 ==========");
                basicDataTypeExample.runAll();
                distributedLockExample.runAll();
                pubSubExample.runAll();
                pipelineExample.runAll();
                expirationExample.runAll();
                luaScriptExample.runAll();
                bitmapAndHyperLogLogExample.runAll();
                log.info("========== 所有 Redis 示例运行完毕 ==========");
            } catch (Exception e) {
                log.error("运行失败", e);
            }
        };
    }
}
