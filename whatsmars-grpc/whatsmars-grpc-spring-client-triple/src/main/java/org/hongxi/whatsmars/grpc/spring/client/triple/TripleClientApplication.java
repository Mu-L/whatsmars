package org.hongxi.whatsmars.grpc.spring.client.triple;

import org.hongxi.whatsmars.dubbo.idl.unary.GreeterGrpc;
import org.hongxi.whatsmars.dubbo.idl.unary.GreeterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.client.ImportGrpcClients;

/**
 * gRPC client -> Triple server (whatsmars-dubbo-idl-provider)
 */
@SpringBootApplication
@ImportGrpcClients(basePackages = "org.hongxi.whatsmars.dubbo.idl.unary")
public class TripleClientApplication {
	private static final Logger logger = LoggerFactory.getLogger(TripleClientApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TripleClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(GreeterGrpc.GreeterBlockingStub stub) {
		return args -> {
			logger.info("{}", stub.greet(GreeterRequest.newBuilder().setName("Alien").build()));
		};
	}

}