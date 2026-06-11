package org.hongxi.whatsmars.boot.sample.redis;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shenhongxi on 2017/6/26.
 */
public record User(String name, Integer age, Date createDate) implements Serializable {
    private static final long serialVersionUID = 4064009692985107575L;
}
