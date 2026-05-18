package org.hongxi.whatsmars.redis.client;

import org.hongxi.whatsmars.redis.client.readwrite.ReadWriteRedisClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import redis.clients.jedis.*;

/**
 * Created by javahongxi on 2017/6/23.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "classpath:spring-redis.xml")
public class Demo {

    @Autowired
    @Qualifier("singletonRedisClient")
    private JedisPool singletonRedisClient;

    @Autowired
    private ReadWriteRedisClient readWriteRedisClient;

//    @Autowired
    @Qualifier("redisClusterClient")
    private JedisCluster jedisCluster;

    @Test
    public void testSingleton() {
        try (Jedis jedis = singletonRedisClient.getResource()) {
            jedis.set("model", "Qwen3.6 Plus");
            String cacheContent = jedis.get("model");
            System.out.println(cacheContent);
            jedis.del("model");
        }
    }

    @Test
    public void testReadWrite() {
        readWriteRedisClient.set("model", "Qwen3.6 Plus");
        String cacheContent = readWriteRedisClient.get("model");
        System.out.println(cacheContent);
        readWriteRedisClient.del("model");
    }

//    @Test
    public void testCluster() {
        String cacheContent = jedisCluster.get("model");
        System.out.println(cacheContent);
    }

}
