package org.hongxi.whatsmars.jedis.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 发布/订阅（Pub/Sub）示例
 *
 * 核心概念：
 * - subscribe：订阅者监听频道，收到消息后回调
 * - publish：发布者向频道发送消息
 * - psubscribe：支持通配符的频道订阅（如 news.*）
 *
 * 注意：subscribe 是阻塞操作，必须在独立线程中运行，且需使用独立连接。
 */
@Component
public class PubSubExample {

    private static final Logger log = LoggerFactory.getLogger(PubSubExample.class);

    private static final String CHANNEL_NEWS = "channel:news";
    private static final String CHANNEL_ORDER = "channel:order";

    private final JedisPool jedisPool;

    public PubSubExample(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void runAll() {
        log.info("========== 发布/订阅示例 ==========");
        basicPubSubDemo();
        patternSubscribeDemo();
        log.info("========== 发布/订阅示例结束 ==========\n");
    }

    // ==================== 基础发布/订阅 ====================

    /**
     * 基础发布/订阅：订阅两个频道，然后发布消息
     */
    private void basicPubSubDemo() {
        log.info("--- 基础 Pub/Sub ---");

        CountDownLatch subscriberReady = new CountDownLatch(1);
        CountDownLatch messagesReceived = new CountDownLatch(3); // 预期收到 3 条消息

        // 保留 JedisPubSub 引用，用于从外部主动取消订阅
        JedisPubSub[] pubsubHolder = new JedisPubSub[1];

        // 1. 在独立线程中启动订阅者（阻塞操作）
        Thread subscriberThread = new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                JedisPubSub pubsub = new JedisPubSub() {
                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        log.info("  [订阅者] 已订阅频道: {}, 当前订阅数: {}", channel, subscribedChannels);
                        subscriberReady.countDown();
                    }

                    @Override
                    public void onMessage(String channel, String message) {
                        log.info("  [订阅者] 收到消息 - 频道: {}, 内容: {}", channel, message);
                        messagesReceived.countDown();
                    }

                    @Override
                    public void onUnsubscribe(String channel, int subscribedChannels) {
                        log.info("  [订阅者] 取消订阅频道: {}", channel);
                    }
                };
                pubsubHolder[0] = pubsub;
                jedis.subscribe(pubsub, CHANNEL_NEWS, CHANNEL_ORDER);
            }
        }, "redis-subscriber");
        subscriberThread.setDaemon(true);
        subscriberThread.start();

        try {
            // 2. 等待订阅者就绪
            subscriberReady.await(3, TimeUnit.SECONDS);
            Thread.sleep(200); // 确保订阅完全建立

            // 3. 发布者发送消息
            try (Jedis jedis = jedisPool.getResource()) {
                long receivers1 = jedis.publish(CHANNEL_NEWS, "Redis 8.0 发布！");
                log.info("  [发布者] 发送到 {} 个订阅者", receivers1);

                long receivers2 = jedis.publish(CHANNEL_ORDER, "订单 #10001 已创建");
                log.info("  [发布者] 发送到 {} 个订阅者", receivers2);

                long receivers3 = jedis.publish(CHANNEL_NEWS, "Redis 性能调优指南");
                log.info("  [发布者] 发送到 {} 个订阅者", receivers3);
            }

            // 4. 等待所有消息被接收
            messagesReceived.await(5, TimeUnit.SECONDS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 5. 通过 unsubscribe() 主动取消订阅，连接干净退出 Pub/Sub 模式后再归还池中
        JedisPubSub pubsub = pubsubHolder[0];
        if (pubsub != null && pubsub.isSubscribed()) {
            pubsub.unsubscribe();
        }
        waitForThread(subscriberThread);
        log.info("  订阅已取消");
    }

    // ==================== 通配符订阅（PSubscribe）====================

    /**
     * 通配符订阅：使用 psubscribe 订阅 news.* 模式的所有频道
     */
    private void patternSubscribeDemo() {
        log.info("--- 通配符订阅（PSubscribe）---");

        CountDownLatch subscriberReady = new CountDownLatch(1);
        CountDownLatch messagesReceived = new CountDownLatch(2);

        JedisPubSub[] pubsubHolder = new JedisPubSub[1];

        Thread subscriberThread = new Thread(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                JedisPubSub pubsub = new JedisPubSub() {
                    @Override
                    public void onPSubscribe(String pattern, int subscribedChannels) {
                        log.info("  [通配订阅者] 模式: {}, 订阅数: {}", pattern, subscribedChannels);
                        subscriberReady.countDown();
                    }

                    @Override
                    public void onPMessage(String pattern, String channel, String message) {
                        log.info("  [通配订阅者] 匹配模式: {}, 频道: {}, 内容: {}", pattern, channel, message);
                        messagesReceived.countDown();
                    }
                };
                pubsubHolder[0] = pubsub;
                jedis.psubscribe(pubsub, "channel:news:*");
            }
        }, "redis-psubscriber");
        subscriberThread.setDaemon(true);
        subscriberThread.start();

        try {
            subscriberReady.await(3, TimeUnit.SECONDS);
            Thread.sleep(200);

            // 发布到匹配 channel:news:* 模式的频道
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish("channel:news:tech", "AI 技术突破");
                jedis.publish("channel:news:finance", "股市大涨");
                // 这个不匹配 channel:news:* 模式，不会被通配订阅者收到
                jedis.publish("channel:news", "这条不会匹配 news:*");
            }

            messagesReceived.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 通过 punsubscribe() 主动取消通配符订阅
        JedisPubSub pubsub = pubsubHolder[0];
        if (pubsub != null && pubsub.isSubscribed()) {
            pubsub.punsubscribe();
        }
        waitForThread(subscriberThread);
        log.info("  通配符订阅已取消");
    }

    /**
     * 等待订阅线程正常结束，确保连接干净归还池中
     */
    private void waitForThread(Thread thread) {
        try {
            thread.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
