package org.hongxi.whatsmars.spring.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for monitoring: logs execution time and invocation count.
 *
 * <p>When applied to a method, the {@link MonitorAspect} will intercept the call
 * and log the tag, start time, and elapsed time.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Monitor {

    /**
     * Unique identifier for this monitored method.
     */
    String tag();
}
