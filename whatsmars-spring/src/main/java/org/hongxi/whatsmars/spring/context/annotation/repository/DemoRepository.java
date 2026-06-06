package org.hongxi.whatsmars.spring.context.annotation.repository;

import org.springframework.stereotype.Repository;

/**
 * Demonstrates {@code @Repository} stereotype annotation
 * and automatic component scanning registration.
 */
@Repository
public class DemoRepository {

    public DemoRepository() {
        System.out.println("[DemoRepository] instantiated by Spring");
    }

    public void query() {
        System.out.println("[DemoRepository] query() executed");
    }

    @Override
    public String toString() {
        return "DemoRepository@" + Integer.toHexString(hashCode());
    }
}
