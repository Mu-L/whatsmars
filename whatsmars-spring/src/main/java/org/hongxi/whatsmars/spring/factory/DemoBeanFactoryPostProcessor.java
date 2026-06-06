package org.hongxi.whatsmars.spring.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Demonstrates {@link BeanFactoryPostProcessor}:
 * runs <b>before</b> any bean is instantiated, allowing modification of
 * {@link BeanDefinition} metadata (property values, scope, lazy-init, etc.).
 *
 * <p>This example overrides the {@code cnName} property of the "xmlMars" bean
 * defined in spring-context.xml, showing that bean definitions can be altered
 * before instantiation.</p>
 *
 * @see org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 */
public class DemoBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (beanFactory.containsBeanDefinition("mars")) {
            BeanDefinition bd = beanFactory.getBeanDefinition("mars");
            // Override the cnName property before the bean is instantiated
            bd.getPropertyValues().add("cnName", "火星(PostProcessor修改)");
            System.out.println("[BeanFactoryPostProcessor] modified mars.cnName");
        }
    }
}
