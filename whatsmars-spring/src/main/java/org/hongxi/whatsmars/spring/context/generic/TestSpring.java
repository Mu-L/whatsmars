package org.hongxi.whatsmars.spring.context.generic;

import org.hongxi.whatsmars.spring.model.Earth;
import org.hongxi.whatsmars.spring.model.Mars;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Demonstrates {@link GenericApplicationContext} with mixed bean definition sources:
 * <ul>
 *   <li>{@link XmlBeanDefinitionReader} - load beans from XML</li>
 *   <li>{@link BeanDefinitionBuilder} - programmatic bean registration</li>
 * </ul>
 *
 * <p>{@code GenericApplicationContext} is the most flexible context implementation:
 * it can combine XML, annotation, and programmatic configuration in a single context.</p>
 */
public class TestSpring {

    public static void main(String[] args) {
        GenericApplicationContext ctx = new GenericApplicationContext();

        // 1. Load bean definitions from XML
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new ClassPathResource("spring-context.xml"));

        // 2. Programmatically register a bean definition
        BeanDefinition earthDef = BeanDefinitionBuilder.genericBeanDefinition(Earth.class)
                .addPropertyValue("age", 500)
                .addPropertyValue("cnName", "地球")
                .getBeanDefinition();
        ctx.registerBeanDefinition("earth", earthDef);

        ctx.refresh();

        System.out.println("===== From XML =====");
        Mars mars = ctx.getBean(Mars.class);
        System.out.println("Mars: cnName=" + mars.getCnName() + ", age=" + mars.getAge());

        System.out.println("\n===== From Programmatic Registration =====");
        Earth earth = ctx.getBean(Earth.class);
        System.out.println("Earth: cnName=" + earth.getCnName() + ", age=" + earth.getAge());

        ctx.close();
    }
}
