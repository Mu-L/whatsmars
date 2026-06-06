package org.hongxi.whatsmars.spring.context;

import org.hongxi.whatsmars.spring.model.Mars;
import org.hongxi.whatsmars.spring.task.DemoTask;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;

/**
 * Demonstrates {@link ClassPathXmlApplicationContext}:
 * <ul>
 *   <li>Loading XML bean definitions from the classpath</li>
 *   <li>Retrieving beans by name and type</li>
 *   <li>Using infrastructure beans defined in XML (e.g. ThreadPoolTaskExecutor)</li>
 *   <li>Graceful shutdown via {@code ctx.close()}</li>
 * </ul>
 */
public class TestSpring {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");

        System.out.println("===== Bean by Name =====");
        Mars mars = (Mars) context.getBean("mars");
        System.out.println("mars: age=" + mars.getAge() + ", cnName=" + mars.getCnName());

        System.out.println("\n===== Bean by Type =====");
        ThreadPoolTaskExecutor taskExecutor = context.getBean(ThreadPoolTaskExecutor.class);
        System.out.println("taskExecutor: " + taskExecutor);
        taskExecutor.execute(new DemoTask());

        System.out.println("\n===== All Bean Names =====");
        Arrays.stream(context.getBeanDefinitionNames())
                .forEach(name -> System.out.println("  " + name));

        // Allow the async task to complete before closing
        taskExecutor.getThreadPoolExecutor().shutdown();
        context.close();
    }
}
