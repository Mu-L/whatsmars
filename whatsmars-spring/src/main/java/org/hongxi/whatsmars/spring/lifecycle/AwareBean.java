package org.hongxi.whatsmars.spring.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Demonstrates Spring's {@code Aware} interfaces for injecting container metadata.
 *
 * <p>{@code Aware} callbacks are invoked <b>before</b> {@code BeanPostProcessor.postProcessBeforeInitialization()},
 * which means they run before {@code @PostConstruct}. Common Aware interfaces:
 * <ul>
 *   <li>{@link BeanNameAware} — injects the bean's name in the container</li>
 *   <li>{@link BeanFactoryAware} — injects the owning BeanFactory</li>
 *   <li>{@link ApplicationContextAware} — injects the ApplicationContext</li>
 * </ul>
 */
@Component
public class AwareBean implements BeanNameAware, BeanFactoryAware, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(AwareBean.class);

    @Override
    public void setBeanName(String name) {
        logger.info("BeanNameAware.setBeanName('{}')", name);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        logger.info("BeanFactoryAware.setBeanFactory() — class: {}",
                beanFactory.getClass().getSimpleName());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        logger.info("ApplicationContextAware.setApplicationContext() — id: {}",
                applicationContext.getId());
    }
}
