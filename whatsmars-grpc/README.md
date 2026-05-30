## [gRPC](https://github.com/grpc/grpc-java)
A high performance, open source universal RPC framework.

### User Guide
1. 推荐引入`grpc-netty-shaded`而非`grpc-netty`，避免可能的netty版本冲突
2. 尽管api模块引入了`grpc-stub`和`grpc-protobuf`，但推荐server和client模块仍然显式引入这两个依赖
3. spring-grpc 1.1.0 开始，starter已迁移至 Spring Boot 4.1.x，groupId,artifactId已改变
4. grpcurl
```shell
grpcurl -plaintext localhost:50051 list
```
```shell
grpcurl -plaintext -d '{"name": "lily"}' localhost:50051 helloworld.Greeter/SayHello
```