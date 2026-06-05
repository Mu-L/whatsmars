package org.hongxi.whatsmars.spring.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates {@code @Bean(initMethod, destroyMethod)} for beans
 * that are not Spring-aware (no interface implementation needed).
 *
 * <p>This is useful for third-party classes where you cannot add
 * {@code @PostConstruct} or implement {@code InitializingBean}.
 */
public class ExternalService {
    private static final Logger logger = LoggerFactory.getLogger(ExternalService.class);

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Custom init method, specified via {@code @Bean(initMethod = "connect")}.
     * Runs after {@code @PostConstruct} and {@code afterPropertiesSet()}.
     */
    public void connect() {
        logger.info("[3] @Bean(initMethod) — connecting service '{}'", name);
    }

    /**
     * Custom destroy method, specified via {@code @Bean(destroyMethod = "disconnect")}.
     * Runs after {@code @PreDestroy} and {@code DisposableBean.destroy()}.
     */
    public void disconnect() {
        logger.info("[6] @Bean(destroyMethod) — disconnecting service '{}'", name);
    }
}
