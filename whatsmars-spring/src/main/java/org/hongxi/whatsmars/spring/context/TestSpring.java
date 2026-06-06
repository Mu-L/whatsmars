package org.hongxi.whatsmars.spring.context;

import org.hongxi.whatsmars.spring.context.annotation.service.DemoService;
import org.hongxi.whatsmars.spring.model.Mars;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;

/**
 * Demonstrates {@link ClassPathXmlApplicationContext}:
 * <ul>
 *   <li>Loading XML bean definitions from the classpath</li>
 *   <li>Retrieving beans by name and type</li>
 *   <li>List all bean names</li>
 * </ul>
 */
public class TestSpring {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");

        System.out.println("===== Bean by Name =====");
        Mars mars = (Mars) context.getBean("mars");
        System.out.println("mars: age=" + mars.getAge() + ", cnName=" + mars.getCnName());

        System.out.println("\n===== Bean by Type =====");
        DemoService demoService = context.getBean(DemoService.class);
        System.out.println("demoService: " + demoService);

        System.out.println("\n===== All Bean Names =====");
        Arrays.stream(context.getBeanDefinitionNames())
                .forEach(name -> System.out.println("  " + name));

        context.close();
    }
}
