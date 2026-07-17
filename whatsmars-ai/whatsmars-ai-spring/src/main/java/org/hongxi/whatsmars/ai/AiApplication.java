package org.hongxi.whatsmars.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.awt.*;
import java.net.URI;

/**
 * Spring AI 示例应用启动类
 *
 * @author hongxi
 */
@SpringBootApplication
public class AiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiApplication.class, args);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventListener(Environment environment) {
        return event -> {
            String port = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String accessUrl = "http://localhost:" + port + contextPath;
            System.out.println("\n========================================");
            System.out.println("Application is ready!");
            System.out.println("Open in browser: " + accessUrl);
            System.out.println("========================================\n");
            try {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", accessUrl});
                } else if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", accessUrl});
                } else if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(accessUrl));
                }
            } catch (Exception e) {
                System.out.println("自动打开浏览器失败，请手动访问: " + accessUrl);
            }
        };
    }
}
