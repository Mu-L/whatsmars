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