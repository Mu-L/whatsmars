package org.hongxi.whatsmars.spring.configurer;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Custom {@link Condition} that checks whether a given property
 * is set to {@code true} in the Spring environment.
 *
 * <p>Usage: annotate a {@code @Bean} or {@code @Configuration} class with
 * {@code @Conditional(OnPropertyCondition.class)} and ensure the target
 * property (e.g. {@code feature.cache.enabled}) is {@code true}.</p>
 *
 * @see ConditionalConfig
 */
public class OnPropertyCondition implements Condition {

    /**
     * The property key to check. Hard-coded here for demonstration;
     * in a real project you would read this from an annotation attribute.
     */
    private static final String PROPERTY_KEY = "feature.cache.enabled";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String value = context.getEnvironment().getProperty(PROPERTY_KEY);
        boolean matched = "true".equalsIgnoreCase(value);
        System.out.printf("[OnPropertyCondition] %s=%s -> %s%n",
                PROPERTY_KEY, value, matched ? "MATCH" : "NO MATCH");
        return matched;
    }
}
