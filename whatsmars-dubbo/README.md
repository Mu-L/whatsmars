# [Apache Dubbo](http://dubbo.apache.org)
一款云原生微服务开发框架

## Dubbo 部署经典架构图
![service-governance](service_governance.png)
Dubbo3 引入了应用级服务发现模型，该模型下接口级别的配置信息由消费者与提供者之间自行协商同步，
不再由注册中心负责同步，从而大大减少了注册中心地址同步压力。

## Dubbo 扩展点全景图
![SPI](SPI.png)
