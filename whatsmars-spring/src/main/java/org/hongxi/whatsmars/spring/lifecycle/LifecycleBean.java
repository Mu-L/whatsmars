package org.hongxi.whatsmars.spring.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * Demonstrates {@link SmartLifecycle} for beans that need to auto-start/stop
 * with the ApplicationContext, independent of individual bean lifecycle.
 *
 * <p>{@code SmartLifecycle} is useful for:
 * <ul>
 *   <li>Starting background threads (message listeners, schedulers)</li>
 *   <li>Managing external resource connections</li>
 *   <li>Any component that should auto-start when the context is ready</li>
 * </ul>
 *
 * <p>Key differences from bean lifecycle callbacks:
 * <ul>
 *   <li>{@code start()} runs after <b>all</b> beans are initialized and context is refreshed</li>
 *   <li>{@code stop()} runs when the context is closing, before bean destruction callbacks</li>
 *   <li>{@code getPhase()} controls ordering: lower phase starts first, stops last</li>
 *   <li>{@code isAutoStartup()} controls whether start() is called automatically</li>
 * </ul>
 */
@Component
public class LifecycleBean implements SmartLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(LifecycleBean.class);

    private volatile boolean running = false;

    @Override
    public void start() {
        running = true;
        logger.info("SmartLifecycle.start() — background service started");
    }

    @Override
    public void stop() {
        running = false;
        logger.info("SmartLifecycle.stop() — background service stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Phase 0 (default). Lower values start earlier and stop later.
     * Use Integer.MIN_VALUE for critical infrastructure beans.
     */
    @Override
    public int getPhase() {
        return 0;
    }

    /**
     * Return true to have the container call {@link #start()} automatically
     * during context refresh. Return false for manual start.
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }
}
