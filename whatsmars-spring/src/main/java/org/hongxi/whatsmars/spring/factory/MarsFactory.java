package org.hongxi.whatsmars.spring.factory;

import org.hongxi.whatsmars.spring.model.Mars;

/**
 * Demonstrates factory-method bean creation:
 * <ul>
 *   <li><b>Static factory method</b> — {@code @Bean} or XML {@code factory-method}
 *       calls a static method directly</li>
 *   <li><b>Instance factory method</b> — calls a method on an existing bean instance</li>
 * </ul>
 */
public class MarsFactory {

    /**
     * Static factory method: no instance of MarsFactory needed.
     */
    public static Mars createStatic() {
        System.out.println("[MarsFactory] static createStatic() called");
        return new Mars(300, "火星(StaticFactory)");
    }

    /**
     * Instance factory method: requires a MarsFactory bean in the context.
     */
    public Mars createInstance() {
        System.out.println("[MarsFactory] instance createInstance() called");
        return new Mars(400, "火星(InstanceFactory)");
    }
}
