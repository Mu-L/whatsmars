package org.hongxi.whatsmars.spring;

import org.hongxi.whatsmars.spring.model.Mars;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by shenhongxi on 2016/7/5.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("/spring-context.xml")
public class SpringTest {
    @Autowired
    private Mars mars;

    @Test
    public void hi() {
        assertEquals(45, mars.getAge());
    }
}
