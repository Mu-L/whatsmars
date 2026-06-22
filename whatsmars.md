# 一个 Java 后端工程师的技术全景图：whatsmars 项目深度解读

> **Spring Boot 3.5** | **Java 17+** | **15 大技术模块** | **40+ 子模块** | **覆盖 Java 后端全生态**

在 Java 后端开发的江湖里，每个工程师都面临同一个挑战：**技术栈太广，深入太难**。Spring Boot、Dubbo、gRPC、RocketMQ、Kafka、Redis、Elasticsearch、Sentinel、Nacos、ShardingSphere…… 每一个都是必备技能，但官方文档分散、示例零碎、版本兼容问题层出不穷。

如果你渴望有一个项目，能**系统性地串联起 Java 后端的核心技术栈**，让你在一个工程里亲手实践从 RPC 通信到消息队列、从流量控制到 AI 集成的全链路场景 —— 那么 [whatsmars](https://github.com/javahongxi/whatsmars) 正是为此而生。

## 🎯 为什么需要 whatsmars？

### 痛点一：技术广度与深度的矛盾

一个合格的 Java 后端工程师，至少需要掌握：
- **RPC 框架**：Dubbo、gRPC
- **消息中间件**：RocketMQ、Kafka、Pulsar、RabbitMQ
- **缓存与存储**：Redis、Elasticsearch
- **微服务治理**：Nacos、Sentinel
- **网络编程**：Netty
- **数据库中间件**：ShardingSphere
- **分布式调度**：ElasticJob、XXL-Job
- **AI 集成**：Spring AI、LangChain4j

每一项技术都有官方示例，但**它们散落在不同的仓库、不同的版本、不同的文档里**。当你尝试集成时，才发现：
- 依赖版本冲突让人抓狂
- 不同框架之间的协作方式无从下手
- 生产级别的配置和最佳实践无处可寻

### 痛点二：Spring Boot 3.x 时代的适配焦虑

Spring Boot 3.x 带来了一系列重大变更：
- Java 17 成为最低要求
- Jakarta EE 替代 Java EE（`javax.*` → `jakarta.*`）
- Spring Security、Observability API 大重构
- 虚拟线程（Virtual Threads）的全新编程模型

**大量基于 Spring Boot 2.x 的教程和示例已不再适用**，开发者急需一套基于最新版本的完整参考。

### 痛点三：AI 时代的技术融合

2025-2026 年，AI 能力从"锦上添花"变成了"必备技能"。Java 工程师需要回答新的问题：
- Spring AI 和 LangChain4j 如何选型？
- MCP（Model Context Protocol）Server 怎么搭建？
- AI 工具调用（Tool Calling）如何与现有系统集成？

**这些问题，在 whatsmars 中都能找到答案。**

## 🏗️ 项目全景：15 大模块，一张技术地图

whatsmars 不是一个简单的"Demo 集合"，而是一个**按技术组件精心划分的研究项目**。每个模块都聚焦一个核心领域，子模块之间相互独立又可协作。

| 模块 | 子模块数 | 核心技术 | 说明                           |
|------|---------|---------|------------------------------|
| **whatsmars-ai** | 3 | Spring AI, LangChain4j, MCP | AI 集成双框架对比 + MCP Server      |
| **whatsmars-common** | - | Java 17 | 通用工具：一致性哈希、序列化、MDC 链路        |
| **whatsmars-spring** | - | Spring Framework | Spring 核心特性实践                |
| **whatsmars-spring-boot-samples** | 21 | Spring Boot 3.5 | 覆盖 Web、AOP、缓存、ORM、Tracing 等  |
| **whatsmars-dubbo** | 8 | Dubbo 3.3.6 | RPC + IDL + gRPC 协议对比        |
| **whatsmars-grpc** | 7 | gRPC 1.81.0 | 跨语言 RPC + Spring 原生支持        |
| **whatsmars-mq** | 6 | RocketMQ, Kafka, Pulsar, RabbitMQ | 四大消息中间件全覆盖                   |
| **whatsmars-redis** | - | Redis, Jedis | 缓存、分布式锁、数据结构实战               |
| **whatsmars-elasticsearch** | - | Elasticsearch | 搜索、分析、数据聚合                   |
| **whatsmars-nacos** | - | Nacos | 注册中心 + 配置中心                  |
| **whatsmars-sentinel** | 5 | Sentinel 1.8.10 | 流量控制、熔断降级、多场景适配              |
| **whatsmars-netty** | 14 个子包 | Netty | NIO 网络编程：HTTP、HTTP/2、MessagePack |
| **whatsmars-shardingsphere** | - | ShardingSphere 5.5.2 | 分库分表、读写分离                    |
| **whatsmars-curator** | - | Curator 5.9.0 | ZooKeeper 最佳客户端实践            |
| **whatsmars-scheduling** | 2 | ElasticJob, XXL-Job | 分布式任务调度双方案对比                 |

### 技术栈版本号一览

| 组件 | 版本            |
|------|---------------|
| Java | 17+           |
| Spring Boot | 3.5.14        |
| Spring AI | 1.1.8         |
| LangChain4j | 1.16.3-beta26 |
| Apache Dubbo | 3.3.6         |
| gRPC | 1.81.0        |
| Spring gRPC | 0.12.0        |
| RocketMQ | 5.5.0         |
| Sentinel | 1.8.10        |
| ShardingSphere | 5.5.2         |
| Curator | 5.9.0         |
| Protobuf | 4.34.1        |
| Nacos | 3.2.2     |
| ElasticJob | 3.0.5         |
| XXL-Job | 3.4.0         |

## 🔬 模块深度剖析

### 🤖 whatsmars-ai：双框架对比 + MCP Server

这是项目最具前瞻性的模块。AI 模块没有选择"只押注一个框架"，而是**同时集成了 Spring AI 和 LangChain4j**，让开发者在同一个项目中对比两者的设计理念和使用体验。

**三个子模块各司其职**：

| 子模块 | 说明 |
|--------|------|
| `whatsmars-ai-spring-ai` | Spring AI 原生集成，ChatClient API、Tool Calling |
| `whatsmars-ai-langchain4j` | LangChain4j 集成，链式调用、RAG 模式 |
| `whatsmars-ai-mcp-server` | MCP Server（SSE over WebMVC），暴露 16 个 AI 工具 |

**MCP Server 亮点** —— 通过 `MethodToolCallbackProvider` 将 16 个工具方法统一注册：

- **天气查询**：getWeatherByCity
- **系统工具**：getCurrentDate、getCurrentTime、add、multiply、toUpperCase、toLowerCase、reverseString
- **转换工具**：formatJson、urlEncode/Decode、base64Encode/Decode、stringLength、wordCount

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

**一份工具代码，同时服务于内部 AI 调用和外部 MCP 暴露** —— 这就是优秀架构的优雅设计。

### 📡 whatsmars-dubbo：Dubbo 3.3 全场景实战

Dubbo 模块不只是简单的 Provider/Consumer 示例，而是**覆盖了 Dubbo 3.x 的多种协议和使用模式**：

| 子模块 | 说明                         |
|--------|----------------------------|
| `whatsmars-dubbo-api` | 公共接口定义（契约优先设计）             |
| `whatsmars-dubbo-provider` | 传统 Dubbo 服务提供者             |
| `whatsmars-dubbo-consumer` | 传统 Dubbo 服务消费者             |
| `whatsmars-dubbo-idl` | IDL（Proto）接口定义             |
| `whatsmars-dubbo-idl-provider` | 基于 IDL 的 gRPC 协议 Provider  |
| `whatsmars-dubbo-idl-consumer` | 基于 IDL 的 gRPC 协议 Consumer  |
| `whatsmars-dubbo-consumer-grpc` | 调用 gRPC 服务的 Dubbo Consumer |

**核心设计理念**：
1. **契约优先**：API 接口独立模块，Provider 和 Consumer 通过接口契约通信
2. **协议互通**：Dubbo 的 Triple 协议和 gRPC 协议互通
3. **IDL 驱动**：通过 Proto 文件定义接口，编译时生成代码，类型安全
4. **Sentinel 集成**：Provider 和 Consumer 均集成 `sentinel-apache-dubbo3-adapter`，通过 Nacos 动态配置限流规则，支持接口级和方法级粒度控制：

```json
[
  {
    "resource": "org.hongxi.whatsmars.dubbo.demo.api.DemoService",
    "count": 1000.0,
    "grade": 1,
    "limitApp": "default"
  },
  {
    "resource": "org.hongxi.whatsmars.dubbo.demo.api.DemoService:sayHello(java.lang.String)",
    "count": 10.0,
    "grade": 1,
    "limitApp": "dubbo-demo-consumer"
  }
]
```

这意味着你可以在 Nacos 中**热更新限流规则**，无需重启服务 —— 这正是生产环境中 Dubbo + Sentinel 的最佳实践。

### 🔌 whatsmars-grpc：跨语言 RPC + Spring 原生支持

gRPC 模块展示了从 Proto 定义到完整服务的全流程：

| 子模块 | 说明                       |
|--------|--------------------------|
| `whatsmars-grpc-api` | Proto 文件定义（接口契约）         |
| `whatsmars-grpc-server` | 原生 gRPC Server           |
| `whatsmars-grpc-client` | 原生 gRPC Client           |
| `whatsmars-grpc-spring-client` | Spring 集成 gRPC Client    |
| `whatsmars-grpc-spring-server` | Spring 集成 gRPC Server    |
| `whatsmars-grpc-spring-client-triple` | 调用 Dubbo 服务的 gRPC Client |

**亮点**：通过 `spring-grpc-spring-boot-starter` 实现 Spring Boot 原生 gRPC 支持，不再需要第三方 starter，与 Spring 生态无缝集成。

### 📨 whatsmars-mq：四大消息中间件一网打尽

这是 whatsmars 最"豪华"的模块之一 —— 同时覆盖四种主流消息中间件：

| 子模块 | 中间件                     | 场景                         |
|--------|-------------------------|----------------------------|
| `whatsmars-mq-rocketmq-v5` | RocketMQ 5.x & gRPC 客户端 | 金融级可靠性、事务消息、顺序消息、定时消息      |
| `whatsmars-mq-rocketmq` | RocketMQ 5.x & 原生客户端    | 一些需要集成 `rocketmq-client` 的场景 |
| `whatsmars-mq-kafka` | Kafka                   | 高吞吐消息生产消费                  |
| `whatsmars-mq-kafka-multi` | Kafka（多集群）              | 跨集群消息同步                    |
| `whatsmars-mq-pulsar` | Pulsar                  | 云原生消息平台                    |
| `whatsmars-mq-rabbitmq` | RabbitMQ                | AMQP 标准消息                  |

**为什么覆盖四种？** 因为不同的业务场景需要不同的消息中间件：
- **RocketMQ**：金融级消息可靠性，事务消息
- **Kafka**：大数据日志采集、高吞吐场景
- **Pulsar**：存算分离、多租户
- **RabbitMQ**：企业级消息路由、灵活 Exchange

在一个项目中对比学习，远比分散查阅文档高效得多。

### 🛡️ whatsmars-sentinel：流量控制全场景适配

Sentinel 模块展示了流量控制在不同技术栈中的适配方式：

| 子模块 | 说明 |
|--------|------|
| `whatsmars-sentinel-basic` | Sentinel 核心功能：流控、熔断、系统保护 |
| `whatsmars-sentinel-aop` | AOP 注解方式集成 |
| `whatsmars-sentinel-webmvc` | Spring WebMVC 适配 |
| `whatsmars-sentinel-httpclient` | Apache HttpClient 适配 |
| `whatsmars-sentinel-file-datasource` | 文件数据源动态规则 |

从基础 API 到 AOP 注解、从 WebMVC 到 HttpClient、从硬编码到动态数据源 —— **5 个子模块，5 种集成姿势**，让你全面掌握 Sentinel 的适配技巧。

### 🌐 whatsmars-netty：NIO 网络编程宝典

Netty 模块包含 14 个子包，从入门到进阶，覆盖协议、编解码、安全等多个维度：

| 类别 | 子包 | 说明 |
|------|------|------|
| **基础入门** | discard、echo、objectecho | 最简 Server/Client、对象序列化传输 |
| **协议设计** | factorial、msgpack | 自定义 BigInteger 编解码器；**MessagePack 二进制协议**，自定义 Encoder/Decoder，高效序列化 |
| **HTTP/1.1** | http/helloworld、http/snoop、http/file、http/upload | 最简 HTTP 服务、请求全解析、静态文件服务、文件上传 |
| **HTTP/2** | http2/server、http2/client | **HTTP/2 多路复用**，支持 ALPN 协议协商 + HTTP/1.1 降级（`Http2OrHttpHandler`） |
| **安全通信** | securechat | TLS/SSL 加密聊天，学习 Netty 安全传输层 |
| **进阶技巧** | portunification、localecho、telnet、uptime、redis、nio | 端口统一（多协议同端口）、本地回环、Telnet 客户端、Redis 协议实现、NIO 原生对比 |

### 🎓 whatsmars-spring-boot-samples：21 个 Spring Boot 实战样本

这可能是全网最全面的 Spring Boot 3.x 示例集合：

| 类别 | 子模块 |
|------|--------|
| **Web** | web、webflux、thymeleaf |
| **数据** | datasource、mybatis、mybatis-plus、redis、mongodb、elasticsearch |
| **AOP/DI** | aop、beans |
| **异步/并发** | async、virtual-thread |
| **可观测性** | actuator、tracing、opentelemetry |
| **日志** | logback、log4j2 |
| **其他** | cache、session、test |

每一个子模块都可以独立运行，拿来即用。**特别是 `virtual-thread` 模块** —— 展示了 Java 21+ 虚拟线程在 Spring Boot 中的实际应用，这在其他项目中极为罕见。

### 🧰 whatsmars-common：生产级工具箱

common 模块不是简单的"Utils 集合"，而是包含了**分布式系统核心算法的生产级实现**：

- **一致性哈希**（`consistenthash`）：ConsistentHashRouter + VirtualNode，可用于分布式缓存路由、负载均衡
- **多种序列化方案**（`serialize`）：FastJson2、Hessian2、Protobuf、Java 原生 —— 对比不同序列化方案的性能和特点
- **MDC 链路追踪**（`mdc`）：MDCRunnable、MDCCallable、MDCTool —— 解决异步场景下日志链路丢失问题
- **统一结果封装**（`result`）：Result + ResultHelper —— 标准化的 API 响应格式
- **加解密工具**（`util`）：AES 加密、断言工具等

### ⏰ whatsmars-scheduling：分布式调度双方案

| 子模块 | 说明 |
|--------|------|
| `whatsmars-scheduling-elastic` | ElasticJob —— 基于 ZooKeeper 的去中心化调度 |
| `whatsmars-scheduling-xxl` | XXL-Job —— 轻量级中心化调度平台 |

两种方案各有优劣：ElasticJob 无需独立调度中心但依赖 ZK，XXL-Job 功能丰富但需要独立部署。**在同一个项目中对比，帮你做出最适合的技术选型。**

### 🔴 whatsmars-redis：不只是缓存

Redis 模块远不止基本的 key-value 操作，而是**系统性地演示了 Redis 的 7 种高级用法**：

| 示例 | 说明 |
|------|------|
| `BasicDataTypeExample` | String、Hash、List、Set、ZSet 五大数据类型全演示 |
| `BitmapAndHyperLogLogExample` | 位图（签到、活跃统计）+ HyperLogLog（UV 统计） |
| `DistributedLockExample` | **分布式锁实现**，解决并发场景下的数据竞争问题 |
| `ExpirationExample` | 过期策略与淘汰机制，理解 Redis 内存管理 |
| `LuaScriptExample` | Lua 脚本原子操作，保证多命令执行的原子性 |
| `PipelineExample` | Pipeline 批量操作，大幅降低网络往返时延 |
| `PubSubExample` | 发布/订阅模式，实现轻量级消息通信 |

基于 Jedis 客户端，配置类 `JedisConfig` 封装连接池管理，拿来即用。

### 🔍 whatsmars-elasticsearch：搜索、分析、聚合

Elasticsearch 模块完整覆盖了 ES 的核心操作场景：

| 示例 | 说明 |
|------|------|
| `IndexManagementExample` | 索引创建、映射配置、别名管理 |
| `DocumentCrudExample` | 文档增删改查，基于 `Product` 实体类 |
| `QueryExample` | 全文检索、范围查询、布尔组合、高亮显示 |
| `AggregationExample` | **数据聚合**：分组统计、平均值、最大最小值 |
| `BulkOperationExample` | 批量操作，高效导入大批量数据 |

从索引管理到聚合分析，一个模块掌握 Elasticsearch 的核心能力。

### 🌐 whatsmars-nacos：注册中心 + 配置中心

Nacos 模块演示了配置读取的**三种方式**和服务注册发现的完整流程：

| Controller | 说明                                                    |
|-----------|-------------------------------------------------------|
| `ValueConfigController` | `@Value` 注解注入 Nacos 配置                                |
| `AnnotationConfigController` | `@NacosConfig` 注解方式                                   |
| `BeanConfigController` | `@ConfigurationProperties` 绑定为 `AgentProperties` Bean |
| `NacosManagerController` | `NacosConfigManager` API 操作，支持动态监听配置变更                |
| `NamingController` | **服务注册与发现**：注册服务实例、订阅服务、负载均衡获取实例                      |

特别是 `NacosConfigManager` 方式 —— 可以在运行时**动态监听配置变更并热更新**，这正是生产环境配置中心的最佳实践。

### 🗂️ whatsmars-shardingsphere：分库分表实战

ShardingSphere 模块以「订单」为业务场景，演示分布式数据库中间件的核心能力：

```
OrderController
    └── OrderService
            └── OrderMapper（MyBatis）
                    └── ShardingSphere JDBC（透明分库分表）
```

**核心配置**：在 `application.yml` 中定义分片规则（按用户 ID 哈希分表、按时间分库），业务代码完全无感知 —— 这正是 ShardingSphere「透明化」设计的价值所在。

### 🦓 whatsmars-curator：ZooKeeper 最佳客户端

Curator 是 ZooKeeper 最流行的 Java 客户端。本模块不仅演示了 Curator 的高级用法，还内置了 `EmbeddedZookeeper`（内嵌 ZK），**无需单独安装 ZooKeeper 即可运行所有示例**：

| 示例 | 说明 |
|------|------|
| `CuratorFrameworkExample` | Curator Framework 基础 API：节点增删改查 |
| `DistributedLockExample` | **分布式锁**：基于 ZK 临时节点实现可重入锁 |
| `DistributedAtomicExample` | 分布式原子计数器 |
| `LeaderElectionExample` | **主节点选举**：多实例竞争 Leader，故障自动切换 |
| `PathCacheExample` | Path Cache：本地缓存 ZK 节点变更，避免频繁轮询 |
| `ServiceDiscoveryExample` | **服务注册与发现**：基于 ZK 实现轻量级注册中心 |

分布式锁和主节点选举是分布式系统的经典问题 —— 这两个示例值得深入研究。

### 🌱 whatsmars-spring：Spring 框架核心机制

Spring 模块不是「Spring Boot 快速入门」，而是**深入 Spring 框架底层机制**，拆解 5 大核心子系统：

| 子包 | 说明 |
|------|------|
| `aspect` | AOP 实战：自定义 `@Monitor` 注解 + `MonitorAspect` 实现方法耗时监控 |
| `configurer` | 条件装配：`@Conditional`、`OnPropertyCondition`、`DataSourceProperties` 绑定 |
| `context` | IoC 容器：注解驱动配置（`@Configuration`）vs XML 泛型注入 |
| `converter` | 类型转换：自定义 `StringToMarsConverter`，掌握 Spring 类型转换体系 |
| `event` | 事件机制：`OrderCreatedEvent` + `OrderEventListener`，解耦业务逻辑 |
| `factory` | Bean 工厂：`BeanFactoryPostProcessor` 扩展 Bean 创建流程 |

理解这些底层机制，才能写出真正优雅的 Spring 应用。

## 🏛️ 架构设计哲学

### 1. 按技术组件划分，而非按业务划分

大多数示例项目按业务功能（用户、订单、支付）划分模块。whatsmars 反其道而行 —— **按技术组件划分**，每个模块聚焦一个中间件或框架。

这种设计的优势：
- **按需学习**：想学 RocketMQ？直接看 `whatsmars-mq-rocketmq`
- **独立运行**：每个模块可以独立编译、独立运行，互不干扰
- **版本解耦**：不同技术组件可以使用各自最适合的版本

### 2. 统一依赖管理，告别版本冲突

根 POM 统一管理 40+ 个第三方依赖版本，通过 `<dependencyManagement>` 确保所有模块的依赖一致性：

```xml
<!-- BOM 导入 —— gRPC 生态版本统一 -->
<dependency>
    <groupId>io.grpc</groupId>
    <artifactId>grpc-bom</artifactId>
    <version>${grpc.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>

<!-- 精确控制间接依赖版本 —— 避免传递依赖冲突 -->
<dependency>
    <groupId>org.javassist</groupId>
    <artifactId>javassist</artifactId>
    <version>3.30.2-GA</version>
</dependency>
```

甚至连 `javassist`、`zstd-jni`、`snappy-java` 这些容易被忽略的间接依赖都做了版本锁定 —— **这就是生产级项目的严谨态度**。

### 3. 对比式设计，加深技术理解

whatsmars 在多个关键领域提供了**双方案或多方案对比**：
- **AI**：Spring AI vs LangChain4j
- **RPC**：Dubbo vs gRPC
- **消息**：RocketMQ vs Kafka vs Pulsar vs RabbitMQ
- **调度**：ElasticJob vs XXL-Job
- **序列化**：FastJson vs Hessian2 vs Protobuf vs Java 原生

不是"选一个最好的"，而是**让你在对比中理解每种方案的适用场景**。

### 4. 与 spring-cloud-samples 互补

whatsmars 聚焦于**单个技术组件的深度研究**，而姊妹项目 [spring-cloud-samples](https://github.com/javahongxi/spring-cloud-samples) 聚焦于**微服务架构的全链路整合**。两者互为补充：

```
whatsmars：深挖每个组件 ──▶ 理解"怎么用"
spring-cloud-samples：串联多个组件 ──▶ 理解"怎么组合"
```

## 🎓 学习价值

### 对于初中级开发者
- ✅ 系统性学习 Java 后端核心技术栈
- ✅ 掌握 Spring Boot 3.5 最佳实践
- ✅ 理解 Dubbo、gRPC、消息队列等中间件的使用方式
- ✅ 21 个 Spring Boot 子模块，即学即用

### 对于高级开发者
- ✅ 多 RPC 框架对比（Dubbo vs gRPC，IDL 驱动设计）
- ✅ 四大消息中间件选型参考
- ✅ Sentinel 多场景适配方案（AOP / WebMVC / HttpClient）
- ✅ 一致性哈希、MDC 链路追踪等分布式算法实现
- ✅ AI 双框架对比 + MCP Server 搭建

### 对于架构师
- ✅ 技术选型参考：同一项目中对比不同方案的优劣
- ✅ 依赖版本管理最佳实践（BOM + 间接依赖锁定）
- ✅ 多模块 Maven 项目组织架构设计
- ✅ 从 Spring Boot 2.x 到 3.x 的迁移参考

## 🚦 快速开始

### 前置要求
- Java 17+（推荐 [Eclipse Temurin](https://adoptium.net/zh-CN/temurin/releases)）
- Maven 3.8+

### 三步上手

**1. 克隆项目**
```bash
git clone https://github.com/javahongxi/whatsmars.git
cd whatsmars
```

**2. 编译构建**
```bash
./mvnw clean package
```

**3. 选择感兴趣的模块运行**
```bash
# 体验 AI 模块（MCP Server）
cd whatsmars-ai/whatsmars-ai-mcp-server
mvn spring-boot:run

# 体验 Dubbo RPC
cd whatsmars-dubbo/whatsmars-dubbo-provider
mvn spring-boot:run

# 体验 Spring Boot Web 示例
cd whatsmars-spring-boot-samples/whatsmars-boot-sample-web
mvn spring-boot:run
```

## 💡 最佳实践总结

### 1. 依赖管理的"两层防护"

whatsmars 采用**两层依赖管理策略**：
- **第一层**：继承 `spring-boot-starter-parent`，获得 Spring Boot 生态的统一版本
- **第二层**：在 `<dependencyManagement>` 中精确控制所有第三方依赖版本

这确保了即使引入新的中间件，也不会出现版本冲突。

### 2. 模块独立性设计

每个子模块都遵循以下原则：
- **可独立编译**：不依赖其他子模块的编译产物（除 `whatsmars-common`）
- **可独立运行**：包含完整的 Application 入口和配置文件
- **按需引入**：只引入该技术领域所需的依赖，避免臃肿

### 3. AI 集成的"双轨策略"

不绑定单一框架，而是同时集成 Spring AI 和 LangChain4j：
- Spring AI 更适合与 Spring 生态深度集成的场景
- LangChain4j 提供更丰富的 AI 编排能力（RAG、Memory、Chain）
- MCP Server 作为标准协议层，屏蔽底层框架差异

### 4. 消息中间件的选型指南

通过实际代码对比四种消息中间件：

| 场景 | 推荐中间件 |
|------|-----------|
| 金融级消息、事务消息 | RocketMQ |
| 大数据日志、高吞吐 | Kafka |
| 多租户、存算分离 | Pulsar |
| 企业级路由、灵活 Exchange | RabbitMQ |

## 🔗 相关链接

- 📦 **项目地址**: [https://github.com/javahongxi/whatsmars](https://github.com/javahongxi/whatsmars)
- 🌐 **作者主页**: [hongxi.org](http://hongxi.org)
- ☁️ **Spring Cloud 生态研究**: [spring-cloud-samples](https://github.com/javahongxi/spring-cloud-samples)
- 🔧 **轻量级 RPC 框架**: [jaws](https://github.com/javahongxi/jaws)
- 📖 **Spring Boot**: [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- 🤖 **Spring AI**: [https://spring.io/projects/spring-ai](https://spring.io/projects/spring-ai)
- 🦜 **LangChain4j**: [https://docs.langchain4j.dev](https://docs.langchain4j.dev)
- 📡 **Apache Dubbo**: [https://dubbo.apache.org](https://dubbo.apache.org)
- 🔌 **gRPC**: [https://grpc.io](https://grpc.io)
- 📨 **Apache RocketMQ**: [https://rocketmq.apache.org](https://rocketmq.apache.org)
- 🛡️ **Sentinel**: [https://sentinelguard.io](https://sentinelguard.io)
- 🌐 **Nacos**: [https://nacos.io](https://nacos.io)

## 🤝 贡献指南

欢迎提交 Issue 和 PR！如果你发现：
- 某个技术组件的配置过时或错误
- 有更好的实践方案或配置方式
- 想添加新的技术模块（如 Seata、SkyWalking、MinIO）
- 版本升级带来的兼容性问题

请随时参与贡献，让 whatsmars 成为 Java 后端学习的最佳参考项目！

---

**© [hongxi.org](http://hongxi.org)** | 以技术全景图的方式，系统性地研究 Java 后端生态

---

## 📝 结语

whatsmars 这个名字来源于一句经典语录 —— **"What's Mars?"**，寓意着对未知技术的探索精神。

这个项目的核心价值不在于"大而全"，而在于：

1. **系统性** —— 15 大模块覆盖 Java 后端核心技术栈，不再东拼西凑
2. **对比性** —— 同一领域提供多种方案，在对比中建立深度理解
3. **生产性** —— 统一的依赖管理、严谨的版本控制、生产级的配置实践
4. **前瞻性** —— AI 双框架集成、MCP Server、虚拟线程等前沿技术率先落地

如果你正在：
- 🎯 系统性学习 Java 后端技术栈
- 🚀 准备从 Spring Boot 2.x 升级到 3.x
- 🤖 探索 AI 与 Java 的融合方案
- 🏗️ 为团队搭建技术选型参考库
- 📚 寻找一个可以长期跟进的学习项目

**Star ⭐ whatsmars，开启你的 Java 全栈进阶之旅！**

```bash
git clone https://github.com/javahongxi/whatsmars.git
cd whatsmars
./mvnw clean package
```

让我们一起探索 Java 生态的无限可能！🚀
