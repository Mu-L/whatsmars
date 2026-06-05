package org.hongxi.whatsmars.spring.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Demonstrates {@link BeanPostProcessor} — intercepts bean creation for ALL beans.
 *
 * <p>The two methods wrap around the initialization callbacks:
 * <ol>
 *   <li>{@code postProcessBeforeInitialization()} — runs BEFORE {@code @PostConstruct} and {@code afterPropertiesSet()}</li>
 *   <li>{@code postProcessAfterInitialization()} — runs AFTER {@code @PostConstruct}, {@code afterPropertiesSet()}, and {@code initMethod}</li>
 * </ol>
 *
 * <p>Common use cases:
 * <ul>
 *   <li>Custom annotation processing (Spring's {@code AutowiredAnnotationBeanPostProcessor} works this way)</li>
 *   <li>Proxy wrapping (Spring AOP uses {@code AbstractAutoProxyCreator} which extends BeanPostProcessor)</li>
 *   <li>Injecting custom logic into all beans</li>
 * </ul>
 */
@Component
public class LoggingBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingBeanPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().getPackage().getName().startsWith("org.hongxi.whatsmars.spring.lifecycle")) {
            logger.info("BeanPostProcessor.postProcessBeforeInitialization('{}')", beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().getPackage().getName().startsWith("org.hongxi.whatsmars.spring.lifecycle")) {
            logger.info("BeanPostProcessor.postProcessAfterInitialization('{}')", beanName);
        }
        return bean;
    }
}
