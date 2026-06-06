package org.hongxi.whatsmars.spring.factory;

import org.hongxi.whatsmars.spring.model.Mars;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * Demonstrates low-level {@link DefaultListableBeanFactory} usage <b>without</b>
 * an {@code ApplicationContext}:
 * <ul>
 *   <li>Loading XML bean definitions via {@link XmlBeanDefinitionReader}</li>
 *   <li>Programmatic {@link BeanDefinition} registration via {@link BeanDefinitionBuilder}</li>
 *   <li>Retrieving beans by name and type</li>
 * </ul>
 *
 * <p>{@code DefaultListableBeanFactory} is the core implementation that backs
 * every {@code ApplicationContext}.</p>
 */
public class BeanFactoryTest {

    public static void main(String[] args) {
        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();

        // 1. Load bean definitions from XML
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
        reader.loadBeanDefinitions(new ClassPathResource("spring-context.xml"));

        // 2. Programmatically register an additional bean definition
        BeanDefinition earthDef = BeanDefinitionBuilder.genericBeanDefinition(Mars.class)
                .addPropertyValue("age", 999)
                .addPropertyValue("cnName", "火星(programmatic)")
                .getBeanDefinition();
        bf.registerBeanDefinition("programmaticMars", earthDef);

        System.out.println("===== From XML =====");
        Mars xmlMars = bf.getBean("mars", Mars.class);
        System.out.println("mars: cnName=" + xmlMars.getCnName() + ", age=" + xmlMars.getAge());

        System.out.println("\n===== From Programmatic Registration =====");
        Mars progMars = bf.getBean("programmaticMars", Mars.class);
        System.out.println("programmaticMars: cnName=" + progMars.getCnName() + ", age=" + progMars.getAge());

        System.out.println("\n===== Bean Definition Count =====");
        System.out.println("Total bean definitions: " + bf.getBeanDefinitionCount());
    }
}
