## RocketMQ 5.0

RocketMQ 5.0 严格定义了消息类型，即Normal、FIFO、Delay、Transaction，在Broker开启`enableTopicMessageTypeCheck=true`的情况下，生产或消费的消息类型必须与
Topic指定的消息类型一致，否则Broker会拒绝请求并返回“类型不匹配”错误。

### Quick Start
download [rocketmq-all-5.5.0-bin-release.zip](https://dist.apache.org/repos/dist/release/rocketmq/5.5.0/rocketmq-all-5.5.0-bin-release.zip)
```
> nohup sh bin/mqnamesrv &
> tail -f ~/logs/rocketmqlogs/namesrv.log
> nohup sh bin/mqbroker -n localhost:9876 --enable-proxy &
> tail -f ~/logs/rocketmqlogs/proxy.log
> sh bin/mqshutdown broker
> sh bin/mqshutdown namesrv
```

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