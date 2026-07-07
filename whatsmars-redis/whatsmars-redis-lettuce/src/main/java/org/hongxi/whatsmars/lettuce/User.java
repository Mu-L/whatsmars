package org.hongxi.whatsmars.lettuce;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author javahongxi
 */
public record User(String name, Integer age, Date createDate) implements Serializable {
    @Serial
    private static final long serialVersionUID = 4064009692985107575L;
}