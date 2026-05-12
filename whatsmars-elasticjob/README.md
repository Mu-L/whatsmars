### 分布式调度
https://shardingsphere.apache.org/elasticjob/

#### 最佳实践
- shardingTotalCount不赋值时，即演变为master-slave模式
- 为提高worker工作效率，很多时候worker只负责异步调用服务或发MQ

#### 其他
Mac 安装zk:<br>
官网下载zookeeper，解压至`/usr/local/zookeeper`<br>
编辑`.zshrc`，添加`export PATH="/usr/local/zookeeper/bin:$PATH"`
执行`source .zshrc`<br>
启动`zkServer.sh start`<br>
停止`zkServer.sh stop`