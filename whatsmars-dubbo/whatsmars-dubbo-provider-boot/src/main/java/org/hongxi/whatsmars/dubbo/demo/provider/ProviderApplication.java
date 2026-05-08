package org.hongxi.whatsmars.dubbo.demo.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import java.util.concurrent.CountDownLatch;

/**
 * Created by javahongxi on 2017/12/4.
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = {"org.hongxi.whatsmars.dubbo.demo.provider.service"})
public class ProviderApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ProviderApplication.class, args);
        new CountDownLatch(1).await();
    }
}
