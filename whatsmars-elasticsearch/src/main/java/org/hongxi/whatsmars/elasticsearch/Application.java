package org.hongxi.whatsmars.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Created by shenhongxi on 2018/11/19.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(ElasticsearchClient client) {
        return args -> {
            System.out.println("ElasticsearchClient is ready");
            System.out.println(client.info());
        };
    }
}