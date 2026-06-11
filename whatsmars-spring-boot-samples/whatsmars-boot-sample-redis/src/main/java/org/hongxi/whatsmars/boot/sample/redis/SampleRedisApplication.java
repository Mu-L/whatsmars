package org.hongxi.whatsmars.boot.sample.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;

/**
 * Created by javahongxi on 2017/12/5.
 */
@SpringBootApplication
public class SampleRedisApplication implements CommandLineRunner {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SampleRedisApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        stringRedisTemplate.opsForValue().set("count", "1");
        System.out.println(stringRedisTemplate.opsForValue().get("count"));

        User user = new User("hongxi", 37, new Date());
        redisTemplate.opsForValue().set("user", user);
        User user1 = (User) redisTemplate.opsForValue().get("user");
        if (user1 != null) {
            System.out.println(user1);
        }

        redisTemplate.opsForList().rightPush("list", 1);
        System.out.println(redisTemplate.opsForList().range("list", 0, 2));
    }
}
