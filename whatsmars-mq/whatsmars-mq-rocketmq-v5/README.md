## Apache RocketMQ 5.0
Apache RocketMQ 是一款面向万亿级消息规模的 AI 原生异步通信引擎。诞生于阿里巴巴高并发电商场景，经过数千家企业的生产验证，
RocketMQ 已从高性能消息队列演进为统一消息平台，横跨传统业务消息、事件流处理和新兴的 AI 原生通信三大范式。

RocketMQ 5.0 严格定义了消息类型，即Normal、FIFO、Delay、Transaction，在Broker开启`enableTopicMessageTypeCheck=true`的情况下，生产或消费的消息类型必须与
Topic指定的消息类型一致，否则Broker会拒绝请求并返回“类型不匹配”错误。

### Quick Start
#### Run RocketMQ locally
download [rocketmq-all-5.5.0-bin-release.zip](https://dist.apache.org/repos/dist/release/rocketmq/5.5.0/rocketmq-all-5.5.0-bin-release.zip)
```
> nohup sh bin/mqnamesrv &
> tail -f ~/logs/rocketmqlogs/namesrv.log
> nohup sh bin/mqbroker -n localhost:9876 --enable-proxy &
> tail -f ~/logs/rocketmqlogs/proxy.log
> sh bin/mqshutdown broker
> sh bin/mqshutdown namesrv
```

#### Create Topic and Consumer Group
RocketMQ 5.0 要求必须手动创建Topic和消费组，可使用 mqadmin 工具创建Topic和消费组，也可通过控制台创建Topic和消费组。<br>
```text
git clone https://github.com/apache/rocketmq-dashboard
cd rocketmq-dashboard
mvn spring-boot:run
# 访问http://localhost:8082
# 在控制台创建Topic和消费组
```

本模块boot示例需要创建的Topic和消费组如下

| Topic | 消息类型 | Consumer Group                     |
| --- |------|-------------------------------|
| demo-normal-topic | 普通消息 | my-consumer_demo-normal-topic |
| demo-fifo-topic | 顺序消息 | my-consumer_demo-fifo-topic   |
| demo-delay-topic | 延迟消息 | my-consumer_demo-delay-topic  |
| demo-trans-topic | 事务消息 | my-consumer_demo-trans-topic  |

#### Run Consumer and Producer
```shell
mvn spring-boot:run -Pconsumer
```
```shell
mvn spring-boot:run -Pproducer
```
