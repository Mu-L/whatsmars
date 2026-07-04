## whatsmars-arthas

Arthas 是阿里巴巴开源的 Java 诊断工具，能够在不修改代码、不重启应用的情况下，实时诊断线上问题。本模块通过 Spring Boot Starter 的方式集成 Arthas，并提供一组演示接口，方便学习和实践各类 Arthas 命令。

### 依赖

```xml
<dependency>
    <groupId>com.taobao.arthas</groupId>
    <artifactId>arthas-spring-boot-starter</artifactId>
    <version>${arthas.version}</version>
</dependency>
```

引入 `arthas-spring-boot-starter` 后，Spring Boot 应用启动时会自动 attach 自身进程，无需额外操作。

### 启动

```bash
cd whatsmars-arthas
mvn spring-boot:run
```

启动后各端口说明：

| 端口 | 用途 |
|------|------|
| `8080` | 应用 HTTP 端口 |
| `8563` | Arthas Web Console（浏览器访问 `http://localhost:8563`） |
| `3658` | Arthas Telnet 端口（`telnet 127.0.0.1 3658`） |

### 演示接口与 Arthas 命令对照

#### 1. watch — 观察方法入参、返回值、异常

**接口：**
```
GET /arthas/watch?id=1
GET /arthas/watch?id=-1    # 触发异常
GET /arthas/watch?id=2000  # 返回 null
```

**Arthas 命令：**
```bash
watch org.hongxi.whatsmars.arthas.service.DiagnoseService findUser '{params,returnObj,throwExp}' -x 2
```

#### 2. trace — 追踪方法内部调用链路耗时

**接口：**
```
GET /arthas/trace?orderId=1
```

**Arthas 命令：**
```bash
# 追踪所有调用
trace org.hongxi.whatsmars.arthas.service.DiagnoseService processOrder

# 只打印耗时 > 100ms 的调用
trace org.hongxi.whatsmars.arthas.service.DiagnoseService processOrder '#cost > 100'
```

#### 3. stack — 打印方法被调用的调用链

**接口：**
```
GET /arthas/stack?from=A
GET /arthas/stack?from=B
```

**Arthas 命令：**
```bash
stack org.hongxi.whatsmars.arthas.service.DiagnoseService logAction
```

#### 4. thread — 查看线程信息

**接口：**
```
GET /arthas/thread?iterations=10000000
```

**Arthas 命令：**
```bash
# 列出所有线程
thread

# 列出 CPU 占用最高的 3 个线程
thread -n 3

# 打印指定线程堆栈
thread <threadId>
```

#### 5. sc / sm / jad — 查看类信息、方法列表、反编译

**接口：**
```
GET /arthas/classinfo
```

**Arthas 命令：**
```bash
# 搜索类
sc *DiagnoseService

# 查看类详细信息（ClassLoader 等）
sc -d org.hongxi.whatsmars.arthas.service.DiagnoseService

# 列出类的所有方法
sm org.hongxi.whatsmars.arthas.service.DiagnoseService

# 反编译类
jad org.hongxi.whatsmars.arthas.service.DiagnoseService
```

#### 6. ognl — 调用静态方法、查看 Spring 上下文

**接口：**
```
GET /arthas/ognl?message=hello
```

**Arthas 命令：**
```bash
# 调用静态方法
ognl '@org.hongxi.whatsmars.arthas.service.DiagnoseService@echo("hello arthas")'
```

#### 7. dashboard — 实时系统面板

无需接口触发，直接在 Arthas 终端执行：
```bash
dashboard
```

实时展示线程状态、内存使用、GC 情况。

### 配置说明

`application.yml` 中的 Arthas 配置项：

```yaml
arthas:
  # Telnet 端口，用于 telnet 方式连接 Arthas 终端
  telnet-port: 3658
  # HTTP 端口，用于浏览器访问 Arthas Web Console
  http-port: 8563
  # 禁止 stop 命令，防止误操作关闭 Arthas
  disable-stop: true
```

通过 Actuator 端点可查看 Arthas 运行状态：
```
GET http://localhost:8080/actuator/arthas
```
