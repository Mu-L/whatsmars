1. 克隆源代码[`xxl-job`](https://github.com/xuxueli/xxl-job)，切换到最新的release分支，如`3.4.0-release`
2. 修改admin的application.properties的datasource.username/password
3. 修改admin的logback.xml中的日志路径
4. 按照doc/db/tables_xxl_job.sql创建数据库和表
5. 在admin模块下执行`mvn spring-boot:run`启动调度中心
6. 打开`localhost:8080/xxl-job-admin`，登录调度中心（admin/123456）
7. 启动执行器`whatsmars-xxl-job`
8. 在调度中心创建任务：简单任务，分片广播任务，带参数的任务，生命周期任务
9. 在调度中心启动/停止任务，观察任务执行情况
10. 生命周期任务：进程启动时执行init，进程结束时执行destroy
11. 测试GLUE任务
