package org.hongxi.whatsmars.dubbo.demo.consumer.grpc;

import org.apache.dubbo.config.annotation.DubboReference;
import org.hongxi.whatsmars.grpc.api.helloworld.Greeter;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Triple client -> gRPC server (whatsmars-grpc-spring-server)
 */
@SpringBootApplication
public class GrpcConsumerApplication {

    private static final Logger log = LoggerFactory.getLogger(GrpcConsumerApplication.class);

    @DubboReference(url = "tri://127.0.0.1:50051")
    private Greeter greeter;

    public static void main(String[] args) {
        SpringApplication.run(GrpcConsumerApplication.class, args);
    }

    @Bean
    CommandLineRunner runner() {
        return args -> {
            log.info("Calling gRPC service");
            greeter.sayHello(HelloRequest.newBuilder().setName("lily").build()).getMessage();
        };
    }
}
