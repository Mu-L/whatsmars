package org.hongxi.whatsmars.dubbo.demo.api.vo;

import java.io.Serial;
import java.io.Serializable;

public record EchoRequest(String message) implements Serializable {
    @Serial
    private static final long serialVersionUID = -2080533871181613308L;
}
