package org.hongxi.whatsmars.common.serialize;

import java.io.Serializable;

/**
 * @author shenhongxi 2019/8/5
 */
public record User(String name, int age) implements Serializable {
    private static final long serialVersionUID = -7723128823885218090L;
}