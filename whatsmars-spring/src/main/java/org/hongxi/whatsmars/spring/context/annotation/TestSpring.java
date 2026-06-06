package org.hongxi.whatsmars.spring.context.annotation;

import org.hongxi.whatsmars.spring.model.Earth;
import org.hongxi.whatsmars.spring.model.Mars;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

/**
 * Demonstrates {@link AnnotationConfigApplicationContext}:
 * <ul>
 *   <li>{@code ctx.register()} - register one or more {@code @Configuration} classes</li>
 *   <li>{@code ctx.scan()} - programmatically trigger component scanning</li>
 *   <li>{@code ctx.getBean()} - retrieve beans by type</li>
 *   <li>{@code ctx.getBeanDefinitionNames()} - list all registered bean names</li>
 * </ul>
 *
 * @see AppConfiguration
 * @see JustConfiguration
 */
public class TestSpring {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

        // 1. Register multiple configuration classes
        ctx.register(AppConfiguration.class, JustConfiguration.class);
        // 2. Programmatic component scanning (additional packages)
        // ctx.scan("org.hongxi.whatsmars.spring.context.annotation.repository");
        ctx.refresh();

        System.out.println("===== Bean Retrieval =====");
        Mars mars = ctx.getBean(Mars.class);
        System.out.println("Mars: cnName=" + mars.getCnName() + ", age=" + mars.getAge());
        Earth earth = ctx.getBean(Earth.class);
        System.out.println("Earth: cnName=" + earth.getCnName() + ", age=" + earth.getAge());

        System.out.println("\n===== All Bean Names =====");
        Arrays.stream(ctx.getBeanDefinitionNames())
                .filter(name -> !name.startsWith("org.springframework"))
                .forEach(name -> System.out.println("  " + name));

        ctx.close();
    }
}
