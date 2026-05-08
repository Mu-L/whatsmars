# [ShardingSphere](http://shardingsphere.apache.org)

## 测试验证
启动项目后，调用以下接口进行测试：

### 1. 插入数据
```shell
# userId=1001，取模路由：1001 % 2 = 1 → ds1 库 t_order_1 表
curl -X POST "http://localhost:8080/order/add?userId=1001&amount=99.50"

# userId=1002，取模路由：1002 % 2 = 0 → ds0 库 t_order_0 表
curl -X POST "http://localhost:8080/order/add?userId=1002&amount=150.00"
```

### 2. 查看控制台日志
```text
Logic SQL: INSERT INTO t_order (order_no, user_id, amount, create_time) VALUES (?, ?, ?, ?)
Actual SQL: ds1 ::: INSERT INTO t_order_1 (order_no, user_id, amount, create_time) VALUES (..., 1001, 99.50, ...)
```
可以看到 Actual SQL 中物理表名已经被正确路由

### 3. 验证数据分布
```sql
-- order_db_0 中的 t_order_0 表
SELECT * FROM order_db_0.t_order_0;  -- 应有 userId=1002 的数据

-- order_db_1 中的 t_order_1 表
SELECT * FROM order_db_1.t_order_1;  -- 应有 userId=1001 的数据
```

### 4. 查询接口验证
```shell
curl "http://localhost:8080/order/query?userId=1001"

curl "http://localhost:8080/order/query?userId=1002"
```