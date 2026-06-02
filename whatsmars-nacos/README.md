## 安装
```shell
curl -fsSL https://nacos.io/nacos-installer.sh | bash
```

## 部署
```shell
nacos-setup
```
会自动部署一个单机实例，并创建密码（用户名：nacos），该密码会写入内置数据库。<br>
第一次部署成功后，就可以使用了，之后如果重启请使用脚本
```shell
bin/shutddown.sh
```
```shell
bin/startup.sh -m standalone
```

## Nacos Config 演示

### 演示Nacos注解
nacos console 创建配置<br>
dataId: github.username<br>
content: javahongxi
1. 访问 localhost:8761/config/hello
1. 修改配置后再访问
1. 删除配置后观察日志

### 演示Bean配置和Value注解
nacos console 创建配置<br>
dataId: cloud-agent.properties<br>
content: (Properties 格式)
```properties
cloud.agent.name=Trae CN
cloud.agent.version=3.3.60
cloud.agent.credits=2000000
cloud.agent.enabled=true
cloud.agent.provider.name=Alibaba
cloud.agent.provider.model=Qwen3.7 Plus
cloud.agent.provider.api-key=xxx123aa
```
1. 访问 localhost:8761/config/agent
1. 访问 localhost:8761/config/value
1. 修改配置后再访问

### 演示Nacos原生API
1. 访问 localhost:8761/nacos/listener?dataId=my.city
1. 访问 localhost:8761/nacos/publishConfig?dataId=my.city&content=wuhan
1. 访问 localhost:8761/nacos/getConfig?dataId=my.city
1. 访问 localhost:8761/nacos/removeConfig?dataId=my.city
