## RocketMQ 5.0

RocketMQ 5.0 严格定义了消息类型，即Normal、FIFO、Delay、Transaction，在Broker开启`enableTopicMessageTypeCheck=true`的情况下，生产或消费的消息类型必须与
Topic指定的消息类型一致，否则Broker会拒绝请求并返回“类型不匹配”错误。