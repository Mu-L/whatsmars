## [gRPC](https://github.com/grpc/grpc-java)
A high performance, open source universal RPC framework.

### User Guide
1. 推荐引入`grpc-netty-shaded`而非`grpc-netty`，因为你无法确保gRPC的netty版本刚好与项目中其他组件依赖的netty版本一致
2. 尽管api模块引入了`grpc-stub`和`grpc-protobuf`，但推荐server和client模块仍然显式引入这两个依赖