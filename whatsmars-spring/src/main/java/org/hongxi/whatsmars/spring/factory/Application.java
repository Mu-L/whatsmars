package org.hongxi.whatsmars.spring.factory;

import org.hongxi.whatsmars.spring.model.Mars;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Comprehensive demo of Spring Factory concepts:
 * <ul>
 *   <li>{@link FactoryBean} — {@link MarsFactoryBean}</li>
 *   <li>Static factory method — {@link MarsFactory#createStatic()}</li>
 *   <li>Instance factory method — {@link MarsFactory#createInstance()}</li>
 *   <li>{@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}
 *       — {@link DemoBeanFactoryPostProcessor}</li>
 *   <li>{@link org.springframework.beans.factory.BeanFactoryAware}
 *       — {@link FactoryAwareDemo}</li>
 * </ul>
 */
@Configuration
@ImportResource("classpath:spring-context.xml")
public class Application {

    // ---- FactoryBean ----

    @Bean
    public MarsFactoryBean marsFactoryBean() {
        return new MarsFactoryBean();
    }

    // ---- Static factory method ----

    @Bean
    public Mars staticMars() {
        return MarsFactory.createStatic();
    }

    // ---- Instance factory method ----

    @Bean
    public MarsFactory marsFactory() {
        return new MarsFactory();
    }

    @Bean
    public Mars instanceMars() {
        return marsFactory().createInstance();
    }

    // ---- BeanFactoryPostProcessor ----

    @Bean
    public static DemoBeanFactoryPostProcessor demoBeanFactoryPostProcessor() {
        return new DemoBeanFactoryPostProcessor();
    }

    // ---- BeanFactoryAware ----

    @Bean
    public FactoryAwareDemo factoryAwareDemo() {
        return new FactoryAwareDemo();
    }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(Application.class);
        ctx.refresh();

        System.out.println("===== 1. FactoryBean: getBean returns the PRODUCT =====");
        Mars fromFactoryBean = ctx.getBean("marsFactoryBean", Mars.class);
        System.out.println("  getBean(\"marsFactoryBean\"): " + fromFactoryBean.getCnName());
        FactoryBean<?> factoryItself = ctx.getBean("&marsFactoryBean", FactoryBean.class);
        System.out.println("  getBean(\"&marsFactoryBean\"): " + factoryItself.getClass().getSimpleName());

        System.out.println("\n===== 2. Static factory method =====");
        Mars staticMars = ctx.getBean("staticMars", Mars.class);
        System.out.println("  staticMars: " + staticMars.getCnName() + ", age=" + staticMars.getAge());

        System.out.println("\n===== 3. Instance factory method =====");
        Mars instanceMars = ctx.getBean("instanceMars", Mars.class);
        System.out.println("  instanceMars: " + instanceMars.getCnName() + ", age=" + instanceMars.getAge());

        System.out.println("\n===== 4. BeanFactoryPostProcessor modified XML bean =====");
        Mars xmlMars = ctx.getBean("mars", Mars.class);
        System.out.println("  mars: " + xmlMars.getCnName() + ", age=" + xmlMars.getAge());

        System.out.println("\n===== 5. BeanFactoryAware =====");
        FactoryAwareDemo awareDemo = ctx.getBean(FactoryAwareDemo.class);
        awareDemo.lookupBeans();

        ctx.close();
    }
}
