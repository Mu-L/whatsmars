package org.hongxi.whatsmars.spring.profile;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Demonstrates {@code @Profile}: this bean is only activated when
 * the "prod" profile is active.
 *
 * @see PropertyConfiguration
 */
@Profile("prod")
@Component
public class ConditionalOnProfile implements InitializingBean {

    @Value("${profile}")
    private String profile;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println(profile);
    }
}
