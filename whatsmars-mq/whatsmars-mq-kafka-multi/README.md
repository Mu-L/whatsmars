本地启动启动两套单机kafka集群，操作步骤如下
1. 下载kafka官方包`kafka_2.13-3.9.2.tgz`，解压
2. 拷贝一份命名为`kafka_2.13-3.9.2_backup`
3. 修改server.properties文件`broker.id` `listeners` `log.dirs` `zookeeper.connect`
4. 修改zookeeper.properties文件`dataDir` `clientPort`
5. 启动zk: `bin/zookeeper-server-start.sh config/zookeeper.properties`
6. 启动server: `bin/kafka-server-start.sh config/server.properties`
7. 启动本模块示例

两套单机kafka集群配置如下

| 配置项                 | Cluster 1 (集群一)              | Cluster 2 (集群二)              |
|:--------------------|:-----------------------------|:-----------------------------|
| `broker.id`         | `0` (或 `1`)                  | `1` (或 `2`)                  |
| `listeners`         | `PLAINTEXT://localhost:9092` | `PLAINTEXT://localhost:9093` |
| `log.dirs`          | `/tmp/kafka-logs-cluster1`   | `/tmp/kafka-logs-cluster2`   |
| `zookeeper.connect` | `localhost:2181`             | `localhost:2182`             |
| `dataDir`           | `/tmp/zookeeper-cluster1`    | `/tmp/zookeeper-cluster2`    |
| `clientPort`        | `2181`                       | `2182`                       |

启动server看到如下日志，表示两套集群是独立的<br>
集群一：
```text
Recorded new ZK controller, from now on will use node 10.54.110.97:9092
```
集群二：
```text
Recorded new ZK controller, from now on will use node 10.54.110.97:9093
```