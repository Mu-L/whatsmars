package org.hongxi.whatsmars.redis.client.readwrite;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by javahongxi on 2017/6/22.
 */
public class ReadWriteRedisClient implements InitializingBean, DisposableBean {

    private JedisPool master;

    private List<JedisPool> slaves = new ArrayList<>();

    private JedisPoolConfig jedisPoolConfig;

    // master:port,slave:port,slave:port...
    private String address;

    private int timeout = 3000;

    private Random random = new Random();

    @Override
    public void afterPropertiesSet() throws Exception {
        String[] hostAndPorts = address.split(",");
        String masterHP = hostAndPorts[0];
        String[] ms = masterHP.split(":");
        master = new JedisPool(jedisPoolConfig, ms[0], Integer.valueOf(ms[1]), timeout);
        if (hostAndPorts.length > 1) {
            for (int i = 1; i < hostAndPorts.length; i++) {
                String[] ss = hostAndPorts[i].split(":");
                JedisPool slave = new JedisPool(jedisPoolConfig, ss[0], Integer.valueOf(ss[1]), timeout);
                slaves.add(slave);
            }
        }
        slaves.add(master);
    }

    @Override
    public void destroy() throws Exception {
        if (master != null) {
            master.close();
        }
        for (JedisPool slave : slaves) {
            if (slave != null) {
                slave.close();
            }
        }
    }

    public String get(String key) {
        try (Jedis jedis = fetchResource(true)) {
            return jedis.get(key);
        }
    }

    public List<String> mget(String... keys) {
        try (Jedis jedis = fetchResource(true)) {
            return jedis.mget(keys);
        }
    }

    public String setex(String key,int seconds,String value) {
        try (Jedis jedis = fetchResource(false)) {
            return jedis.setex(key, seconds, value);
        }
    }

    public Long setnx(String key,String value) {
        try (Jedis jedis = fetchResource(false)) {
            return jedis.setnx(key, value);
        }
    }

    public String set(String key,String value) {
        try (Jedis jedis = fetchResource(false)) {
            return jedis.set(key, value);
        }
    }

    public Long del(String key) {
        try (Jedis jedis = fetchResource(false)) {
            return jedis.del(key);
        }
    }

    public Long expire(String key,int seconds) {
        try (Jedis jedis = fetchResource(false)) {
            return jedis.expire(key, seconds);
        }
    }

    public Boolean exists(String key) {
        try (Jedis jedis = fetchResource(false)) {
            return jedis.exists(key);
        }
    }

    public Long exists(String... keys) {
        try (Jedis jedis = fetchResource(false)) {
            return jedis.exists(keys);
        }
    }

    private Jedis fetchResource(boolean read) {
        if(slaves.isEmpty() || !read) {
            return master.getResource();
        }
        int size = slaves.size();
        int i = random.nextInt(size);
        return slaves.get(i).getResource();
    }

    public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
