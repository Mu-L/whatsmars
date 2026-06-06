package org.hongxi.whatsmars.spring.factory;

import org.hongxi.whatsmars.spring.model.Mars;
import org.springframework.beans.factory.FactoryBean;

/**
 * Demonstrates {@link FactoryBean} — one of Spring's most important concepts.
 *
 * <p>A {@code FactoryBean} is <b>not</b> the same as a regular bean:
 * <ul>
 *   <li>{@code getBean("marsFactoryBean")} returns the <b>product</b> ({@link Mars})</li>
 *   <li>{@code getBean("&marsFactoryBean")} returns the <b>factory itself</b></li>
 * </ul>
 *
 * <p>This pattern is widely used internally by Spring (e.g. {@code ProxyFactoryBean},
 * {@code JndiFactoryBean}) and in MyBatis ({@code MapperFactoryBean}).</p>
 */
public class MarsFactoryBean implements FactoryBean<Mars> {

    @Override
    public Mars getObject() {
        System.out.println("[MarsFactoryBean] getObject() — creating Mars instance");
        return new Mars(200, "火星(FactoryBean)");
    }

    @Override
    public Class<?> getObjectType() {
        return Mars.class;
    }
}
