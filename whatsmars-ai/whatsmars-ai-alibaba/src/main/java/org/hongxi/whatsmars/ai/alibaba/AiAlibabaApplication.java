package org.hongxi.whatsmars.ai.alibaba;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.HttpURLConnection;
import java.net.URI;

/**
 * 必须先启动whatsmars-ai-spring，因为依赖了它 的 MCP Server
 */
@SpringBootApplication
public class AiAlibabaApplication {

    private static final String AI_SPRING_HEALTH_URL = "http://localhost:8083/actuator/health";
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_INTERVAL_MS = 2000;

    public static void main(String[] args) {
        if (!checkAiSpringHealth()) {
            System.err.println("\n========================================");
            System.err.println("[ERROR] whatsmars-ai-spring 未启动！");
            System.err.println("请先启动 ai-spring 模块，再启动本模块。");
            System.err.println("健康检查地址: " + AI_SPRING_HEALTH_URL);
            System.err.println("========================================\n");
            System.exit(1);
        }
        SpringApplication.run(AiAlibabaApplication.class, args);
    }

    /**
     * 检测 ai-spring 模块是否已启动（通过 actuator/health 端点）
     * 支持重试，最多重试 {@link #MAX_RETRIES} 次，每次间隔 {@link #RETRY_INTERVAL_MS}ms
     */
    private static boolean checkAiSpringHealth() {
        System.out.println("正在检测 ai-spring 模块是否已启动: " + AI_SPRING_HEALTH_URL);
        for (int i = 1; i <= MAX_RETRIES; i++) {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(AI_SPRING_HEALTH_URL).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                int responseCode = connection.getResponseCode();
                connection.disconnect();
                if (responseCode == 200) {
                    System.out.println("ai-spring 模块健康检查通过 ✓");
                    return true;
                }
            } catch (Exception e) {
                System.out.println("第 " + i + " 次检测失败: " + e.getMessage());
            }
            if (i < MAX_RETRIES) {
                System.out.println("等待 " + (RETRY_INTERVAL_MS / 1000) + " 秒后重试...");
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventListener(Environment environment) {
        return event -> {
            String port = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String accessUrl = "http://localhost:" + port + contextPath + "/chatui/index.html";
            System.out.println("\n========================================");
            System.out.println("Application is ready!");
            System.out.println("Chat with your agent: " + accessUrl);
            System.out.println("========================================\n");
        };
    }
}
