package org.hongxi.whatsmars.boot.sample.redis;

import org.hongxi.whatsmars.boot.sample.redis.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Created by javahongxi on 2017/12/5.
 */
@SpringBootApplication
public class SampleRedisApplication implements CommandLineRunner {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SampleRedisApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        stringRedisTemplate.opsForValue().set("count", "1");
        System.out.println(stringRedisTemplate.opsForValue().get("count"));

        User user = User.builder().username("javahongxi").nickname("hongxi").build();
        redisTemplate.opsForValue().set("user3", user);
        User user1 = (User) redisTemplate.opsForValue().get("user3");
        if (user1 != null) {
            System.out.println(user1.getUsername());
        }

        redisTemplate.opsForList().rightPush("list", 1);
        System.out.println(redisTemplate.opsForList().range("list", 0, 2));
    }
}
