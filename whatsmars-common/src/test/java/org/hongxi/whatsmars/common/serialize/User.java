package org.hongxi.whatsmars.common.serialize;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author hongxi
 */
public record User(String name, int age) implements Serializable {
    @Serial
    private static final long serialVersionUID = -7723128823885218090L;
}