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
```shell
curl http://localhost:8761/config/hello
```
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
```shell
curl http://localhost:8761/config/agent
```
```shell
curl http://localhost:8761/config/value
```
修改配置后再访问

### 演示Nacos原生API
```shell
curl 'http://localhost:8761/nacos/listener?dataId=my.city'
curl 'http://localhost:8761/nacos/publishConfig?dataId=my.city&content=wuhan'
curl 'http://localhost:8761/nacos/getConfig?dataId=my.city'
curl 'http://localhost:8761/nacos/removeConfig?dataId=my.city'
```

## 服务注册与发现演示
同一个Nacos Client实例，仅能向一个服务注册一个实例；<br>
若同一个Nacos Client实例多次向同一个服务注册实例，后注册的实例将会覆盖先注册的实例。<br>
我们的演示需要注册多个实例，为此我们启动3个Java进程来测试
```shell
java -jar whatsmars-nacos.jar --server.port=8761
java -jar whatsmars-nacos.jar --server.port=8762
java -jar whatsmars-nacos.jar --server.port=8763
```
1. 注册第一个实例
```shell
curl -X POST "http://localhost:8761/nacos/naming/register/simple?serviceName=test-service&ip=192.168.1.100&port=8080"
```
2. 注册第二个实例（带元数据）
```shell
curl -X POST "http://localhost:8762/nacos/naming/register" \
  -d "serviceName=test-service" \
  -d "ip=192.168.1.101" \
  -d "port=8081" \
  -d "metadata[version]=2.0.0"
```
3. 查看所有实例
```shell
curl "http://localhost:8761/nacos/naming/instances/all?serviceName=test-service"
```
4. 查看健康实例
```shell
curl "http://localhost:8761/nacos/naming/instances/healthy?serviceName=test-service"
```
5. 选择一个实例（模拟负载均衡）
```shell
curl "http://localhost:8761/nacos/naming/instances/one?serviceName=test-service"
```
6. 订阅服务变化
```shell
curl -X POST "http://localhost:8761/nacos/naming/subscribe?serviceName=test-service"
```
7. 注册第三个实例（触发订阅事件）
```shell
curl -X POST "http://localhost:8763/nacos/naming/register/simple?serviceName=test-service&ip=192.168.1.102&port=8082"
```
8. 查看所有实例
```shell
curl "http://localhost:8761/nacos/naming/instances/all?serviceName=test-service"
```
9. 注销一个实例
```shell
curl -X DELETE "http://localhost:8761/nacos/naming/deregister?serviceName=test-service&ip=192.168.1.100&port=8080"
```
10. 再次查看所有实例（验证注销成功）
```shell
curl "http://localhost:8761/nacos/naming/instances/all?serviceName=test-service"
```
11. 取消订阅
```shell
curl -X DELETE "http://localhost:8761/nacos/naming/unsubscribe?serviceName=test-service"
```
12. 注销所有实例
```shell
curl -X DELETE "http://localhost:8762/nacos/naming/deregister?serviceName=test-service&ip=192.168.1.101&port=8081"
curl -X DELETE "http://localhost:8763/nacos/naming/deregister?serviceName=test-service&ip=192.168.1.102&port=8082"
```

## nacos server 升级方式
下载最近的二进制包解压，用target下的`nacos-server.jar`覆盖本地的即可
