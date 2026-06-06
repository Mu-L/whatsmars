package org.hongxi.whatsmars.spring.factory;

import org.hongxi.whatsmars.spring.model.Mars;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * Demonstrates {@link BeanFactoryAware}: a bean can obtain a reference to
 * its owning {@link BeanFactory} and then look up other beans programmatically.
 *
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Dynamic bean lookup based on runtime conditions</li>
 *   <li>Prototype bean injection into singleton beans</li>
 *   <li>Custom service locator patterns</li>
 * </ul>
 */
public class FactoryAwareDemo implements BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        System.out.println("[BeanFactoryAware] setBeanFactory() called, factory: "
                + beanFactory.getClass().getSimpleName());
    }

    /**
     * Demonstrates dynamic bean lookup via the stored BeanFactory reference.
     */
    public void lookupBeans() {
        System.out.println("[FactoryAwareDemo] Looking up beans from BeanFactory...");

        // Look up by name (avoids ambiguity when multiple Mars beans exist)
        Mars mars = beanFactory.getBean("mars", Mars.class);
        System.out.println("  mars: cnName=" + mars.getCnName() + ", age=" + mars.getAge());

        // Check if a bean exists
        boolean hasFactory = beanFactory.containsBean("marsFactory");
        System.out.println("  containsBean('marsFactory'): " + hasFactory);
    }
}
