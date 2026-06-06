package org.hongxi.whatsmars.spring.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Demonstrates all five AOP advice types:
 * <ul>
 *   <li>{@code @Before} - runs before the target method</li>
 *   <li>{@code @After} - runs after the target method (finally-like)</li>
 *   <li>{@code @AfterReturning} - runs after successful return</li>
 *   <li>{@code @AfterThrowing} - runs when an exception is thrown</li>
 *   <li>{@code @Around} - wraps the target method, can control execution</li>
 * </ul>
 *
 * <p>Also demonstrates {@code @Pointcut} for reusable pointcut expressions.</p>
 */
@Component
@Aspect
public class MonitorAspect {

    private static final Logger logger = LoggerFactory.getLogger(MonitorAspect.class);

    // ---- Reusable Pointcut ----

    /**
     * Pointcut that matches any method annotated with {@link @Monitor}.
     */
    @Pointcut("@annotation(monitor)")
    public void monitorAnnotatedMethod(Monitor monitor) {}

    // ---- Advice methods ----

    @Before("monitorAnnotatedMethod(monitor)")
    public void beforeAdvice(JoinPoint jp, Monitor monitor) {
        logger.info("[{}] @Before — method: {}", monitor.tag(), jp.getSignature().getName());
    }

    @After("monitorAnnotatedMethod(monitor)")
    public void afterAdvice(JoinPoint jp, Monitor monitor) {
        logger.info("[{}] @After — method: {}", monitor.tag(), jp.getSignature().getName());
    }

    @AfterReturning(pointcut = "monitorAnnotatedMethod(monitor)", returning = "result")
    public void afterReturningAdvice(Monitor monitor, Object result) {
        logger.info("[{}] @AfterReturning — result: {}", monitor.tag(), result);
    }

    @AfterThrowing(pointcut = "monitorAnnotatedMethod(monitor)", throwing = "ex")
    public void afterThrowingAdvice(Monitor monitor, Throwable ex) {
        logger.warn("[{}] @AfterThrowing — exception: {}", monitor.tag(), ex.getMessage());
    }

    @Around("monitorAnnotatedMethod(monitor)")
    public Object aroundAdvice(ProceedingJoinPoint pjp, Monitor monitor) throws Throwable {
        String tag = monitor.tag();
        long start = System.nanoTime();
        logger.info("[{}] @Around — entering", tag);
        try {
            return pjp.proceed();
        } finally {
            long elapsedMicros = (System.nanoTime() - start) / 1_000;
            logger.info("[{}] @Around — exited, cost: {}μs", tag, elapsedMicros);
        }
    }
}
