package org.hongxi.whatsmars.lettuce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Order(3)
@Component
public class ObjectSampleRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ObjectSampleRunner.class);

    private final RedisTemplate<String, Object> defaultRedisTemplate;
    private final RedisTemplate<String, Object> cacheRedisTemplate;
    private final RedisTemplate<String, Object> sessionRedisTemplate;

    public ObjectSampleRunner(RedisTemplate<String, Object> defaultRedisTemplate,
                              RedisTemplate<String, Object> cacheRedisTemplate,
                              RedisTemplate<String, Object> sessionRedisTemplate) {
        this.defaultRedisTemplate = defaultRedisTemplate;
        this.cacheRedisTemplate = cacheRedisTemplate;
        this.sessionRedisTemplate = sessionRedisTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("");
        log.info("========== Multi-Redis Object Read/Write Verification ==========");

        verifyObjectReadWrite("default", defaultRedisTemplate);
        verifyObjectReadWrite("cache", cacheRedisTemplate);
        verifyObjectReadWrite("session", sessionRedisTemplate);

        log.info("========== All object read/write verifications passed! ==========");
    }

    private void verifyObjectReadWrite(String name, RedisTemplate<String, Object> template) {
        try {
            String key = "sample:object:test:" + name;
            User value = new User(name + "-test-user", 25, new Date());

            template.opsForValue().set(key, value);
            Object result = template.opsForValue().get(key);
            template.delete(key);

            log.info("[{}] Object OK: set={}, get={}", name, value, result);
        } catch (Exception e) {
            log.error("[{}] Object FAILED: {}", name, e.getMessage());
        }
    }
}
