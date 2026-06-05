package org.hongxi.whatsmars.grpc.spring.client;

import org.hongxi.whatsmars.grpc.api.helloworld.GreeterGrpc;
import org.hongxi.whatsmars.grpc.api.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.ImportGrpcClients;

@SpringBootApplication
@ImportGrpcClients(basePackages = "org.hongxi.whatsmars.grpc.api.helloworld")
public class GrpcClientApplication {
	private static final Logger logger = LoggerFactory.getLogger(GrpcClientApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(GrpcClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(GreeterGrpc.GreeterBlockingStub stub) {
		return args -> {
			logger.info("{}", stub.sayHello(HelloRequest.newBuilder().setName("Alien").build()));
		};
	}

}