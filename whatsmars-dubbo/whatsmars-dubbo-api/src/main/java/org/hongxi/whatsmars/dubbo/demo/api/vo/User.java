package org.hongxi.whatsmars.dubbo.demo.api.vo;

import java.io.Serial;
import java.io.Serializable;

public record User(String name, int age) implements Serializable {
    @Serial
    private static final long serialVersionUID = 3383972798607883428L;
}
