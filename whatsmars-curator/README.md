Mac 安装zk:<br>
官网下载zookeeper，解压至`/usr/local/zookeeper`<br>
编辑`.zshrc`，添加`export PATH="/usr/local/zookeeper/bin:$PATH"`
执行`source .zshrc`<br>
启动`zkServer.sh start`<br>
停止`zkServer.sh stop`