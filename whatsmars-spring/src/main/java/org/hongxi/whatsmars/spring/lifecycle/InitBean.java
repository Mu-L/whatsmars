package org.hongxi.whatsmars.spring.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Demonstrates Spring bean initialization and destruction callbacks.
 *
 * <p>Execution order:
 * <ol>
 *   <li>{@code @PostConstruct} — Jakarta annotation, runs first</li>
 *   <li>{@code InitializingBean.afterPropertiesSet()} — Spring interface, runs second</li>
 *   <li>{@code @PreDestroy} — Jakarta annotation, runs before destroy()</li>
 *   <li>{@code DisposableBean.destroy()} — Spring interface, runs last</li>
 * </ol>
 */
@Component
public class InitBean implements InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(InitBean.class);

    @PostConstruct
    public void init() {
        logger.info("[1] @PostConstruct");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("[2] InitializingBean.afterPropertiesSet()");
    }

    @PreDestroy
    public void clear() {
        logger.info("[3] @PreDestroy");
    }

    @Override
    public void destroy() throws Exception {
        logger.info("[4] DisposableBean.destroy()");
    }
}
