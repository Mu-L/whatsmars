# 你的技术栈，全球排名多少？——whatsmars 项目技术选型全景解读

> **Spring Boot 3.5** | **Java 17+** | **15 大技术模块** | **40+ 子模块** | **覆盖 Java 后端全生态**

每次技术选型，你是不是也有过这样的纠结？

- "Dubbo 和 gRPC 到底选哪个？"
- "RocketMQ 还是 Kafka？团队总争论不休。"
- "Spring AI 和 LangChain4j，哪个才是 Java AI 的未来？"

这些问题的答案，不在某个官方文档里，而在**全球技术流行度的大趋势**中。

今天，我们换一个角度 —— **用全球排名数据来审视 [whatsmars](https://github.com/javahongxi/whatsmars) 项目的技术选型**，看看这个覆盖了 Java 后端全生态的项目，到底选对了多少。

---

## 📊 一张表看懂：whatsmars 技术栈的全球段位

我们先来一张"硬碰硬"的排名对照表。所有数据均来自 TIOBE、Stack Overflow 2025 Developer Survey、DB-Engines 2026、JetBrains 2025 等权威来源。

| 技术 | whatsmars 版本 | 全球排名 / 地位 | 数据来源 |
|------|---------------|---------------|---------|
| **Java** | 17+ | TIOBE **第3~4名**，企业级后端全球第1语言 | TIOBE 2025 |
| **Spring Boot** | 3.5.14 | Java Web 框架**全球第1**，~75k Stars | GitHub / SO 2025 |
| **MySQL** | - | DB-Engines **全球第2**，SO 使用率 40.5% | DB-Engines 2026 |
| **Redis** | Jedis | Key-Value **全球第1**，SO 使用率 28%，增长最猛 | DB-Engines 2026 |
| **Kafka** | - | 消息队列**全球第1**，大数据流处理标准 | 业界共识 |
| **Elasticsearch** | - | 搜索引擎**全球第1**，全文检索事实标准 | DB-Engines 2026 |
| **gRPC** | 1.81.0 | 跨语言 RPC **全球第1**，~43k Stars | GitHub |
| **Dubbo** | 3.3.6 | RPC 框架**全球 Top 3**，~41.7k Stars | GitHub |
| **Netty** | - | Java 网络编程**事实标准**，~34k Stars | GitHub |
| **Nacos** | 3.2.2 | 注册/配置中心**国内第1**，~32.5k Stars | GitHub |
| **RocketMQ** | 5.5.0 | 消息队列**全球 Top 3**，金融级首选 | 业界共识 |
| **Sentinel** | 1.8.10 | 限流熔断**国内第1**，~22k Stars | GitHub |
| **ShardingSphere** | 5.5.2 | 分库分表**国内第1**，Apache 顶级项目 | Apache |
| **Spring AI** | 1.1.8 | Java AI 框架**全球 Top 2**，采用率 52% | JetBrains 2025 |
| **LangChain4j** | 1.16.3 | Java AI 框架**全球第1**，采用率 68% | JetBrains 2025 |
| **XXL-Job** | 3.4.0 | 分布式调度**国内第1** | 业界共识 |
| **Arthas** | 4.1.3 | Java 诊断工具**全球第1** | 业界共识 |

**结论先行**：whatsmars 的 17 项核心技术中，**5 项全球第1，6 项全球 Top 3，6 项区域霸主** —— 这不是一个 Demo 集合，而是一份**经过深思熟虑的技术选型答卷**。

---

## 🎯 为什么需要 whatsmars？

### 痛点一：技术栈太散，学习成本太高

一个合格的 Java 后端工程师，至少要掌握 RPC、消息队列、缓存、搜索引擎、微服务治理、数据库中间件、AI 集成等 **7 大领域**。

但现实是：
- 每个技术的官方示例分散在不同仓库
- 版本兼容问题让人抓狂
- 不同框架之间的协作方式无从下手

**whatsmars 的解法**：一个项目，15 大模块，40+ 子模块，**统一版本管理，即学即用**。

### 痛点二：技术选型缺乏数据支撑

"我觉得用 RocketMQ 好" —— 这样的选型讨论缺乏说服力。

**whatsmars 的解法**：每个技术选型都经得起全球数据的检验。不是"我觉得"，而是"全球开发者在用"。

### 痛点三：AI 时代的技术焦虑

2025-2026 年，AI 能力从"锦上添花"变成了"必备技能"。Java 工程师急需回答：Spring AI 和 LangChain4j 到底怎么选？

**whatsmars 的解法**：不选 —— **两个都集成**，让你在同一个项目中对比体验。

---

## 🏆 T0 梯队：全球顶级 —— 每个 Java 工程师的必修课

### ✨ Spring Boot 3.5.14 —— 全球 Java Web 框架之王

Spring Boot 以 ~75k GitHub Stars 稳居 Java Web 框架**全球第1**。在 Stack Overflow 2025 开发者调查中，Spring 生态是 Java 开发者使用最广泛的框架。

whatsmars 基于最新的 **Spring Boot 3.5.14**，提供了 **21 个独立可运行的示例模块**：

```
whatsmars-spring-boot-samples/
├── whatsmars-boot-sample-web          # Web MVC
├── whatsmars-boot-sample-webflux      # 响应式编程
├── whatsmars-boot-sample-virtual-thread  # 虚拟线程（Java 21+）
├── whatsmars-boot-sample-redis        # 缓存集成
├── whatsmars-boot-sample-mybatis-plus # ORM 框架
├── whatsmars-boot-sample-opentelemetry   # 可观测性
├── whatsmars-boot-sample-tracing      # 链路追踪
├── ... 还有 14 个模块
```

这意味着：**无论你团队使用 Spring Boot 的哪个方向，都能在这里找到参考**。

### ✨ Redis —— Key-Value 全球第1，增长最猛

DB-Engines 2026 数据显示，Redis 以 147 分稳居 Key-Value 数据库**全球第1**，Stack Overflow 使用率从 20% 跃升至 28%，是**增长最显著的数据库**。

whatsmars 的 Redis 模块不是简单的 `get/set`，而是**系统性地覆盖了 7 种高级用法**：

```java
// 分布式锁 —— 高并发场景的必备能力
public class DistributedLockExample {
    public void tryLock(Jedis jedis, String lockKey, String requestId) {
        // SET key value NX EX —— 原子性加锁
        String result = jedis.set(lockKey, requestId,
            SetParams.setParams().nx().ex(30));
        if ("OK".equals(result)) {
            try {
                // 业务逻辑
            } finally {
                jedis.del(lockKey); // 释放锁
            }
        }
    }
}
```

加上 Bitmap 签到、HyperLogLog UV 统计、Lua 原子操作、Pipeline 批量、Pub/Sub 消息 —— **一个模块掌握 Redis 的全部核心能力**。

### ✨ Kafka —— 全球消息队列之王

Kafka 是大数据领域**全球第1**的消息系统，LinkedIn、Twitter、Uber 的日志管道标配。

whatsmars 不仅集成了 Kafka，还提供了**多集群消息同步**（`whatsmars-mq-kafka-multi`）的高级示例 —— 这在跨数据中心消息同步场景中极为实用。

---

## 🥇 T1 梯队：全球主流 —— 行业标杆，广泛部署

### ✨ Dubbo 3.3 + gRPC 1.81 —— 双 RPC 框架对比

这是 whatsmars 最精妙的设计之一 —— **同时提供 Dubbo 和 gRPC 两套 RPC 方案**，让你在对比中做出最佳选型。

| 维度 | Dubbo 3.3.6 | gRPC 1.81.0 |
|------|-------------|-------------|
| 全球排名 | RPC Top 3（~41.7k Stars） | 跨语言 RPC 第1（~43k Stars） |
| 协议 | Triple（兼容 gRPC）、Dubbo | HTTP/2 + Protobuf |
| 适用场景 | Java 生态微服务 | 跨语言通信 |
| Spring 集成 | 原生支持 | Spring gRPC Starter |

**Dubbo 模块**覆盖了 8 个子模块，包括 IDL（Proto）接口定义、gRPC 协议互通、Sentinel 动态限流：

```java
// Dubbo + Sentinel 集成 —— 通过 Nacos 动态更新限流规则
@DubboService
public class DemoServiceImpl implements DemoService {

    @Override
    @SentinelResource(value = "sayHello",
        blockHandler = "sayHelloBlockHandler")
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
```

**gRPC 模块**覆盖了 7 个子模块，从原生 Server/Client 到 Spring Boot 原生集成，甚至支持**通过 gRPC 协议调用 Dubbo 服务**：

```
whatsmars-grpc-spring-client-triple  # 用 gRPC 客户端调用 Dubbo Triple 协议
```

这意味着：**Dubbo 和 gRPC 不再是二选一，而是协议互通**。

### ✨ Netty —— Java 网络编程事实标准

Netty 以 ~34k Stars 稳居 Java 网络框架**全球第1**，Dubbo、gRPC、Elasticsearch、RocketMQ 底层都依赖 Netty。

whatsmars 的 Netty 模块包含 **14 个子包**，从入门到进阶，堪称一本 Netty 实战教科书：

```
whatsmars-netty/
├── discard/echo          # 入门：最简 Server/Client
├── http/helloworld       # HTTP/1.1 服务
├── http2/server,client   # HTTP/2 多路复用 + ALPN 降级
├── msgpack               # 自定义二进制协议
├── securechat            # TLS/SSL 加密通信
├── redis                 # Redis 协议实现
├── portunified           # 多协议同端口
└── ... 还有 7 个子包
```

### ✨ Elasticsearch —— 搜索引擎全球第1

DB-Engines 2026 数据：Elasticsearch 以 96 分稳居搜索引擎**全球第1**，在全文检索领域没有对手。

whatsmars 的 ES 模块覆盖了从索引管理、文档 CRUD、全文检索到**数据聚合分析**的完整链路：

```java
// 聚合分析 —— 按分类统计商品数量和平均价格
Aggregation aggregation = Aggregation.of("category_agg",
    a -> a.terms(t -> t.field("category"))
        .aggregations(sub -> sub
            .avg("avg_price", avg -> avg.field("price"))
        ));
```

---

## 🌏 T2 梯队：区域霸主 —— 国内绝对主流，全球影响力快速增长

### ✨ Nacos + Sentinel + RocketMQ —— 微服务治理三剑客

这三个组件在国内的地位不可撼动：

| 技术 | 国内地位 | GitHub Stars | 全球趋势 |
|------|---------|-------------|---------|
| Nacos 3.2.2 | 注册/配置中心**国内第1** | ~32.5k | 全球知名度上升 |
| Sentinel 1.8.10 | 限流熔断**国内第1** | ~22k | Spring Cloud Alibaba 核心 |
| RocketMQ 5.5.0 | 金融级消息**国内第1** | ~12k | 全球金融领域采用增长 |

whatsmars 不仅单独演示了每个组件，还展示了它们之间的**协同作战**：

```
Nacos（配置中心）
    └── Sentinel 限流规则（JSON 格式存储）
            └── 动态热更新，无需重启服务
                    └── 保护 Dubbo 服务不被流量洪峰击垮
```

### ✨ ShardingSphere 5.5.2 —— 分库分表国内第1

Apache ShardingSphere 是国内分库分表领域的**绝对王者**，在金融、电商、政务系统中广泛部署。

whatsmars 以「订单」为业务场景，展示了**透明化分库分表**的优雅设计：

```
OrderController
    └── OrderService
            └── OrderMapper（MyBatis）
                    └── ShardingSphere JDBC（透明分库分表）
                            ├── 按用户 ID 哈希分表
                            └── 按时间范围分库
```

业务代码**完全无感知** —— 这正是 ShardingSphere「JDBC 层透明拦截」的核心价值。

---

## 🤖 T3 梯队：Java AI 双子星 —— 最具前瞻性的技术选型

这是 whatsmars 最让人眼前一亮的部分。

2025-2026 年，Java AI 生态迎来爆发。JetBrains 2025 年调研显示，**62% 的企业正在使用 Java 进行 AI 应用开发**。而在 Java AI 框架领域，形成了清晰的"双子星"格局：

| 框架 | 全球排名 | 采用率 | 核心优势 |
|------|---------|--------|---------|
| **LangChain4j** | Java AI **第1** | 68% | 功能最全、框架无关、Agent 最强 |
| **Spring AI** | Java AI **Top 2** | 52% | Spring 官方、工程化最强、学习曲线最低 |

whatsmars **同时集成了这两个框架**，让开发者在同一个项目中对比体验。

### Spring AI 侧：5 行代码接入 AI

```java
@RestController
public class AiChatController {
    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/ai/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt().user(message).call().content();
    }
}
```

支持通义千问 + DeepSeek 双模型共存，通过不同 `ChatClient` Bean 实现模型切换。

### LangChain4j 侧：声明式 AI 服务

```java
@AiService
public interface SimpleAssistant {
    @SystemMessage("你是一个专业的 Java 技术专家。")
    String chat(String userMessage);
}
```

一个注解，一个接口，AI 服务就这么简单。

### MCP Server：一份工具代码，内外两用

whatsmars 的 MCP Server 通过 `MethodToolCallbackProvider` 统一注册 16 个 AI 工具：

```java
@Bean
public ToolCallbackProvider mcpToolProvider(
        WeatherTools weatherTools,
        TimeTools timeTools,
        CalculatorTools calculatorTools, ...) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(weatherTools, timeTools, calculatorTools, ...)
            .build();
}
```

任何 MCP Client（AI 助手、IDE 插件）都可以通过 SSE 端点直接连接：

```json
{
  "mcpServers": {
    "whatsmars-mcp-server": {
      "url": "http://localhost:8080/sse"
    }
  }
}
```

这意味着：**内部 Tool Calling 和外部 MCP 暴露共用同一套工具代码** —— 这就是优秀架构的优雅设计。

---

## 🏛️ 架构设计：为什么 whatsmars 的选型如此靠谱？

```
                    whatsmars 技术架构全景
                    
    ┌─────────────────────────────────────────────────┐
    │              AI 层（T3 前沿）                     │
    │   Spring AI 1.1.8  │  LangChain4j 1.16.3       │
    │              MCP Server（16 工具）               │
    ├─────────────────────────────────────────────────┤
    │           微服务治理层（T2 区域霸主）               │
    │   Nacos 3.2.2  │  Sentinel 1.8.10              │
    ├─────────────────────────────────────────────────┤
    │              RPC 通信层（T1 全球主流）              │
    │   Dubbo 3.3.6  │  gRPC 1.81.0  │  Netty       │
    ├─────────────────────────────────────────────────┤
    │              消息中间件层（T0+T1）                 │
    │   Kafka  │  RocketMQ 5.5  │  Pulsar  │ RabbitMQ│
    ├─────────────────────────────────────────────────┤
    │              数据存储层（T0 全球顶级）              │
    │   MySQL  │  Redis（KV #1）│  ES（搜索 #1）       │
    │          ShardingSphere 5.5.2（分库分表）         │
    ├─────────────────────────────────────────────────┤
    │              基础设施层                           │
    │   ZooKeeper/Curator  │  XXL-Job  │  Arthas     │
    └─────────────────────────────────────────────────┘
```

**三大设计哲学**：

1. **按技术组件划分** —— 每个模块聚焦一个中间件，按需学习，独立运行
2. **对比式设计** —— AI 双框架、RPC 双框架、四大消息队列、双调度方案，在对比中建立深度理解
3. **统一依赖管理** —— 根 POM 锁定 40+ 依赖版本，连 `javassist`、`zstd-jni` 这些间接依赖都不放过

---

## 📊 模块速查表

| 模块 | 核心技术 | 全球排名 | 子模块数 |
|------|---------|---------|---------|
| whatsmars-ai | Spring AI + LangChain4j + MCP | Java AI Top 2 + #1 | 3 |
| whatsmars-dubbo | Dubbo 3.3.6 | RPC 全球 Top 3 | 8 |
| whatsmars-grpc | gRPC 1.81.0 | 跨语言 RPC 全球 #1 | 7 |
| whatsmars-mq | RocketMQ / Kafka / Pulsar / RabbitMQ | MQ 全球 Top 3 + #1 | 6 |
| whatsmars-redis | Redis (Jedis) | KV 全球 #1 | 7 示例 |
| whatsmars-elasticsearch | Elasticsearch | 搜索全球 #1 | 5 示例 |
| whatsmars-netty | Netty | 网络框架全球 #1 | 14 子包 |
| whatsmars-nacos | Nacos 3.2.2 | 注册中心国内 #1 | 5 Controller |
| whatsmars-sentinel | Sentinel 1.8.10 | 限流熔断国内 #1 | 5 |
| whatsmars-shardingsphere | ShardingSphere 5.5.2 | 分库分表国内 #1 | 订单场景 |
| whatsmars-scheduling | ElasticJob + XXL-Job | 调度国内 #1 | 2 |
| whatsmars-curator | Curator 5.9.0 | 分布式协调全球 #1 | 6 示例 |
| whatsmars-spring-boot-samples | Spring Boot 3.5.14 | Web 框架全球 #1 | 21 |
| whatsmars-arthas | Arthas 4.1.3 | 诊断工具全球 #1 | - |

---

## 🎓 学习价值

### 对于初中级开发者
- ✅ 系统性学习 Java 后端 **17 项核心技术**
- ✅ 21 个 Spring Boot 3.5 子模块，即学即用
- ✅ AI 模块快速上手，跟上时代步伐

### 对于高级开发者
- ✅ 多 RPC 框架对比（Dubbo vs gRPC，IDL 驱动 + 协议互通）
- ✅ 四大消息中间件选型参考（RocketMQ / Kafka / Pulsar / RabbitMQ）
- ✅ AI 双框架对比 + MCP Server 搭建
- ✅ 一致性哈希、MDC 链路追踪等分布式算法实现

### 对于架构师
- ✅ 技术选型参考：每个选型都有全球数据支撑
- ✅ 依赖版本管理最佳实践（BOM + 间接依赖锁定）
- ✅ 多模块 Maven 项目组织架构设计
- ✅ 从 Spring Boot 2.x 到 3.x 的迁移参考

---

## 🚦 快速开始

### 前置要求
- Java 17+（推荐 [Eclipse Temurin](https://adoptium.net/zh-CN/temurin/releases)）
- Maven 3.8+

### 三步上手

```bash
# 1. 克隆项目
git clone https://github.com/javahongxi/whatsmars.git
cd whatsmars

# 2. 一键编译（内置 Maven Wrapper，无需单独安装 Maven）
./mvnw clean package

# 3. 选择感兴趣的模块运行
# 体验 AI MCP Server
cd whatsmars-ai/whatsmars-ai-spring && mvn spring-boot:run

# 体验 Dubbo RPC
cd whatsmars-dubbo/whatsmars-dubbo-provider && mvn spring-boot:run

# 体验 Spring Boot Web
cd whatsmars-spring-boot-samples/whatsmars-boot-sample-web && mvn spring-boot:run
```

---

## 💡 技术选型的"黄金法则"

从 whatsmars 的技术栈中，我们可以提炼出三条选型法则：

### 1. 优先选择全球排名前列的技术

不是"最新的"最好，而是"用得最多的"最稳。Spring Boot、Redis、Kafka 之所以长期霸榜，是因为它们经过了全球数百万开发者的验证。

### 2. 同领域至少了解两种方案

Dubbo vs gRPC、RocketMQ vs Kafka、Spring AI vs LangChain4j —— 不是"选一个最好的"，而是在对比中理解**适用场景**。whatsmars 的对比式设计正是这一理念的体现。

### 3. AI 能力是 2026 年的必修课

62% 的企业已在使用 Java 进行 AI 开发。Spring AI + LangChain4j 双框架集成 + MCP Server，是 Java 开发者切入 AI 赛道的最佳路径。

---

## 🔗 相关链接

- 📦 **项目地址**: [https://github.com/javahongxi/whatsmars](https://github.com/javahongxi/whatsmars)
- 🌐 **作者主页**: [hongxi.org](http://hongxi.org)
- ☁️ **Spring Cloud 生产级实战**: [spring-cloud-samples](https://github.com/javahongxi/spring-cloud-samples)
- 🔧 **基于 Java 17 和 Netty 的高性能 RPC 框架**: [jaws](https://github.com/javahongxi/jaws)
- 📖 **Spring AI**: [https://spring.io/projects/spring-ai](https://spring.io/projects/spring-ai)
- 🦜 **LangChain4j**: [https://docs.langchain4j.dev](https://docs.langchain4j.dev)
- 📡 **Apache Dubbo**: [https://dubbo.apache.org](https://dubbo.apache.org)
- 🛡️ **Sentinel**: [https://sentinelguard.io](https://sentinelguard.io)

---

## 📝 结语

回到开头的问题 —— **你的技术栈，全球排名多少？**

whatsmars 用 15 个模块、40+ 子模块、17 项核心技术给出了一个清晰的答案：

- **5 项全球第1**：Spring Boot、Redis、Kafka、Elasticsearch、gRPC
- **6 项全球 Top 3**：Java、Dubbo、Netty、RocketMQ、Spring AI、LangChain4j
- **6 项区域霸主**：Nacos、Sentinel、ShardingSphere、XXL-Job、Arthas、Curator

这不是一个"大而全"的 Demo 集合，而是一份**用全球数据验证过的技术选型答卷**。

**如果你正在**：
- 🎯 系统性学习 Java 后端技术栈
- 🤖 探索 Java AI 集成方案（Spring AI / LangChain4j）
- 🏗️ 为团队搭建技术选型参考库
- 📚 寻找一个可以长期跟进的学习项目

**Star ⭐ [whatsmars](https://github.com/javahongxi/whatsmars)，用全球验证过的技术栈，武装你的 Java 后端能力！**

```bash
git clone https://github.com/javahongxi/whatsmars.git
cd whatsmars
./mvnw clean package
```

---

**© [hongxi.org](http://hongxi.org)**
