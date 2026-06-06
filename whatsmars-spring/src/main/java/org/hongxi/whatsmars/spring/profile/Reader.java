package org.hongxi.whatsmars.spring.profile;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Reads the {@code profile} property injected by
 * {@link PropertyConfiguration} and prints it on initialization.
 */
@Component
public class Reader implements InitializingBean {

    @Value("${profile}")
    private String profile;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(profile);
    }
}
