# whatsmars-redis

Redis 学习与实践模块，覆盖三大主流 Java Redis 客户端：**Jedis**、**Lettuce**、**Redisson**，通过可运行的示例帮助深入理解各客户端的使用方式与核心特性。

> **Note**：`whatsmars-redis-lettuce` 模块依赖的 [multi-redis-spring-boot-starter](https://github.com/javahongxi/multi-redis-spring-boot-starter) 
> 是本人开发的 Spring Boot Starter，支持从单个应用连接多个 Redis 实例/集群，提供 Builder + 注解和零代码自动注册两种模式，
> 兼容官方 Spring Boot Redis 配置格式。

## 模块结构

```
whatsmars-redis
├── whatsmars-redis-jedis       # Jedis 客户端示例（基础数据类型、分布式锁、Pub/Sub、Pipeline、Lua 等）
├── whatsmars-redis-lettuce     # Lettuce 客户端示例（多集群接入、Builder/注解模式、响应式）
└── whatsmars-redis-redission   # Redisson 客户端示例（分布式锁、分布式数据结构）
```

## 模块说明

### whatsmars-redis-jedis

基于 **Jedis**（同步阻塞 I/O）的 Redis 基础示例，涵盖以下场景：

| 示例类                           | 说明                                    |
|-------------------------------|---------------------------------------|
| `BasicDataTypeExample`        | 5 种基本数据类型操作：String、List、Set、Hash、ZSet |
| `DistributedLockExample`      | 基于 SETNX + Lua 脚本实现的分布式锁              |
| `PubSubExample`               | 发布/订阅模式（Pub/Sub）                      |
| `PipelineExample`             | Pipeline 批量操作，减少网络往返                  |
| `ExpirationExample`           | 过期策略与内存淘汰机制演示                         |
| `LuaScriptExample`            | Lua 脚本执行示例                            |
| `BitmapAndHyperLogLogExample` | Bitmap 位图与 HyperLogLog 基数统计           |

**依赖**：`spring-boot-starter` + `jedis`

### whatsmars-redis-lettuce

基于 **Lettuce**（异步非阻塞 I/O）及 `multi-redis-spring-boot-starter` 的多集群接入示例，演示如何在一个应用中连接多个 Redis 实例/集群。

**依赖**：`multi-redis-spring-boot-starter`（Lettuce 封装）

支持两种使用模式：

#### Mode 1 — Builder + 注解（代码控制）

通过 `RedisTemplateBuilder` 手动定义 Bean，或使用 `@RedisCluster("name")` 注解注入：

```java
@Configuration
public class SampleConfig {

    @Bean
    public RedisTemplate<String, Object> orderRedisTemplate(RedisTemplateBuilder builder) {
        return builder.cluster("order")
                .keySerializer(RedisSerializer.string())
                .valueSerializer(RedisSerializer.json())
                .hashKeySerializer(RedisSerializer.string())
                .hashValueSerializer(RedisSerializer.json())
                .build();
    }

    @Bean
    public StringRedisTemplate orderStringRedisTemplate(RedisTemplateBuilder builder) {
        return builder.stringTemplate("order");
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> orderReactiveRedisTemplate(RedisTemplateBuilder builder) {
        return builder.reactiveTemplate("order");
    }
}
```

#### Mode 2 — Auto Register（零代码）

设置 `spring.data.redis.auto-register=true`，Bean 自动注册，命名规则：`{clusterName}RedisTemplate`、`{clusterName}StringRedisTemplate`。

```java
@RestController
public class MyController {

    private final RedisTemplate<String, Object> orderRedisTemplate;

    public MyController(RedisTemplate<String, Object> orderRedisTemplate) {
        this.orderRedisTemplate = orderRedisTemplate;
    }
}
```

#### 示例 Runner

| Runner                      | 说明                                 |
|-----------------------------|------------------------------------|
| `ConnectionSampleRunner`    | 连接验证：查询各实例的 `INFO server`，证明实际连接目标 |
| `ReadWriteSampleRunner`     | 读写验证：set/get/delete 往返测试           |
| `AnnotationSampleRunner`    | `@RedisCluster` 注解注入演示（Mode 1）     |
| `ReactiveRedisSampleRunner` | 响应式 `ReactiveRedisTemplate` 操作演示   |

#### 配置示例

```yaml
spring:
  data:
    redis:
      auto-register: true          # false 则使用 Builder 模式
      clusters:
        order:                     # Standalone → orderRedisTemplate
          host: localhost
          port: 6379
          serializer:
            key: string
            value: json
            hash-key: string
            hash-value: json
          lettuce:
            pool:
              max-active: 16
              max-idle: 8
              min-idle: 2
        user:                      # Standalone → userRedisTemplate
          host: localhost
          port: 6380
        cache:                     # Redis Cluster → cacheRedisTemplate
          cluster:
            nodes: localhost:7001,localhost:7002,localhost:7003
            max-redirects: 3
          timeout: 5000ms
          lettuce:
            pool:
              max-active: 16
              max-idle: 8
              min-idle: 2
              max-wait: 10000ms
            cluster:
              refresh:
                adaptive: true
                period: 2000ms
        session:                   # Redis Cluster → sessionRedisTemplate
          cluster:
            nodes: localhost:7011,localhost:7012,localhost:7013
```

> 更多配置说明请参考 [multi-redis-spring-boot-starter README](https://github.com/javahongxi/multi-redis-spring-boot-starter)

### whatsmars-redis-redission

基于 **Redisson**（分布式数据结构与分布式锁）的示例，演示 Redisson 的高级抽象能力。

**依赖**：`redisson-spring-boot-starter`

| 功能      | 说明                                |
|---------|-----------------------------------|
| 分布式锁    | `RLock.tryLock()` 实现互斥访问，支持超时自动释放 |
| 分布式 Map | `RMap` 分布式哈希结构，跨 JVM 共享数据         |

## 前置准备

### 单机 Redis（order & user）

```bash
brew install redis

# 启动默认 Redis（端口 6379）
brew services start redis

# 启动第二个实例（端口 6380）
redis-server --port 6380 --daemonize yes --logfile /tmp/redis-6380.log

# 停止
redis-cli -p 6380 shutdown
```

### Redis Cluster（cache & session）

```bash
# 启动所有集群
./redis-cluster.sh start

# 启动指定集群
./redis-cluster.sh start cache      # 7001-7006 (3 主 + 3 从)
./redis-cluster.sh start session    # 7011-7016 (3 主 + 3 从)

# 查看状态
./redis-cluster.sh status

# 停止集群
./redis-cluster.sh stop all
```

## 运行示例

```bash
# Jedis 示例
cd whatsmars-redis-jedis
mvn spring-boot:run

# Lettuce 多集群示例（需先启动 Redis 实例）
cd whatsmars-redis-lettuce
mvn spring-boot:run

# Redisson 示例
cd whatsmars-redis-redission
mvn spring-boot:run
```

### 预期输出（Lettuce 模块）

```
========== Multi-Redis Connection Verification ==========
[order]   Config -> localhost:6379
[order]   Server -> tcp_port=6379, redis_version=8.0.1
[user]    Config -> localhost:6380
[user]    Server -> tcp_port=6380, redis_version=8.0.1
[cache]   Config -> CLUSTER nodes=[localhost:7001, localhost:7002, localhost:7003]
[cache]   Server -> CLUSTER nodes={127.0.0.1:7001=7001, 127.0.0.1:7002=7002, 127.0.0.1:7003=7003}, redis_version=8.0.1
[session] Config -> CLUSTER nodes=[localhost:7011, localhost:7012, localhost:7013]
[session] Server -> CLUSTER nodes={127.0.0.1:7011=7011, 127.0.0.1:7012=7012, 127.0.0.1:7013=7013}, redis_version=8.0.1
========== Connection verification complete ==========

========== Multi-Redis Read/Write Verification ==========
[order]   Read/Write OK: set=User[name=order-user, age=20, ...], get=User[name=order-user, age=20, ...]
[user]    Read/Write OK: set=hello-user-..., get=hello-user-...
[cache]   Read/Write OK: set=User[name=cache-user, age=20, ...], get=User[name=cache-user, age=20, ...]
[session] Read/Write OK: set=hello-session-..., get=hello-session-...
========== All read/write verifications passed! ==========
```

## 三大客户端对比

| 特性             | Jedis             | Lettuce           | Redisson           |
|----------------|-------------------|-------------------|--------------------|
| I/O 模型         | 同步阻塞（BIO）         | 异步非阻塞（NIO）        | 异步非阻塞（NIO）         |
| 线程安全           | 非线程安全，需连接池        | 线程安全，单连接可复用       | 线程安全               |
| 连接池            | 必须（commons-pool2） | 可选（commons-pool2） | 内置连接管理             |
| 集群支持           | 支持（JedisCluster）  | 支持（原生）            | 支持（原生）             |
| 响应式            | 不支持               | 支持（Reactive API）  | 支持（RxJava）         |
| 分布式数据结构        | 需手动实现             | 需手动实现             | 开箱即用（RLock、RMap 等） |
| Lua 脚本         | 支持                | 支持                | 支持                 |
| Spring Boot 默认 | 否                 | 是（2.x 起）          | 否                  |

## Redis 集群方案

目前国内 Redis 集群的主流方案主要分为三大类，其中 **Redis Cluster（官方原生集群）** 是目前生产环境中最主流的首选方案。

### 1. 官方原生 Redis Cluster（生产主流首选）

Redis 3.0 之后官方推出的原生分布式集群方案，互联网和大型企业项目中最广泛采用的标准方案。

- **核心特点**：无中心化 P2P 架构，16384 个哈希槽（SLOT）动态分片，节点间 Gossip 协议通信
- **优势**：无代理层，性能损耗极低；原生支持自动故障转移和在线扩缩容（Resharding）
- **局限**：需要客户端支持集群协议（Smart Client）；不支持跨槽多 Key 操作
- **适用场景**：高并发、大数据量、需要水平扩展的生产环境

### 2. 云托管集群方案（企业级推荐）

阿里云、腾讯云等提供的托管版 Redis Cluster，底层基于原生 Cluster 或深度定制架构（如阿里云 Tair、腾讯 Tendis）。

- **优势**：自动故障转移、自动备份、一键扩容，具备 SLA 保证，大幅降低运维成本
- **局限**：成本相对较高，底层架构不可控
- **适用场景**：希望免去运维烦恼、追求高可用与稳定性的中大型企业

### 3. 主从复制 + Sentinel 哨兵（中小型系统标配）

1 个主节点 + N 个从节点，配合奇数个 Sentinel 节点进行监控与自动故障转移。

- **优势**：部署简单，成熟稳定，对客户端几乎零侵入
- **局限**：写操作仍是单点，无法突破单机内存上限
- **适用场景**：QPS 2~3 万以内、数据量几十 GB 以内、读多写少且不需横向扩展的业务

### 4. 代理型集群（Codis / Twemproxy）（历史方案）

- **Twemproxy**：轻量级代理，静态分片，不支持动态扩缩容
- **Codis**：企业级动态分片代理，支持可视化 Dashboard，但组件复杂，新项目已较少采用

**总结建议**：新建互联网项目或中大型系统，首选 **Redis Cluster** 或 **云托管 Redis 集群**；中小型业务或读多写少场景，**主从 + 哨兵** 依然是性价比最高的选择。

## 缓存三大经典问题

| 问题   | 现象                 | 解决方案                     |
|------|--------------------|--------------------------|
| 缓存穿透 | 查询不存在的数据，绕过缓存直击 DB | 布隆过滤器拦截 + 缓存空值           |
| 缓存击穿 | 热点 Key 过期瞬间大量请求涌入  | 互斥锁（Redisson 分布式锁）+ 逻辑过期 |
| 缓存雪崩 | 大量 Key 同时过期        | 过期时间加随机值 + 多级缓存架构 + 缓存预热 |

## License

Apache License 2.0

&copy; [hongxi.org](http://hongxi.org)
