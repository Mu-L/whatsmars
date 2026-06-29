package org.hongxi.whatsmars.ai.openai.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring AI 示例应用启动类
 *
 * @author hongxi
 */
@SpringBootApplication
public class AiApplication {

    private static final Logger log = LoggerFactory.getLogger(AiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }

    /**
     * 应用启动后执行 Hello World 演示
     */
    @Bean
    CommandLineRunner cli(ChatClient.Builder builder) {
        return args -> {
            var chat = builder.build();
            log.info("\n=== Spring AI Hello World! ===");
            log.info("USER: Tell me a joke");
            String response = chat.prompt("Tell me a joke").call().content();
            log.info("ASSISTANT: {}", response);
            log.info("=== Hello World demo completed! ===\n");
        };
    }
}
