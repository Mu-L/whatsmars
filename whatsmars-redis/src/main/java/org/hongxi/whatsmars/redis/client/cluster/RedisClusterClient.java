package org.hongxi.whatsmars.redis.client.cluster;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.Connection;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by javahongxi on 2017/6/22.
 */
public class RedisClusterClient implements FactoryBean<JedisCluster>, InitializingBean, DisposableBean {

    private JedisCluster jedisCluster;

    private GenericObjectPoolConfig<Connection> poolConfig;

    // ip:port,ip:port
    private String address;

    private int timeout = 3000;

    @Override
    public JedisCluster getObject() throws Exception {
        return jedisCluster;
    }

    @Override
    public Class<?> getObjectType() {
        return JedisCluster.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Set<HostAndPort> hostAndPorts = buildHostAndPorts();
        jedisCluster = new JedisCluster(hostAndPorts, timeout, poolConfig);
    }

    private Set<HostAndPort> buildHostAndPorts() {
        String[] hostPorts = address.split(",");
        Set<HostAndPort> hostAndPorts = new HashSet<HostAndPort>();
        for(String item : hostPorts) {
            String[] hostPort = item.split(":");
            HostAndPort hostAndPort = new HostAndPort(hostPort[0],Integer.valueOf(hostPort[1]));
            hostAndPorts.add(hostAndPort);
        }
        return hostAndPorts;
    }

    @Override
    public void destroy() throws Exception {
        if (jedisCluster != null) {
            jedisCluster.close();
        }
    }

    public void setPoolConfig(GenericObjectPoolConfig<Connection> poolConfig) {
        this.poolConfig = poolConfig;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
