package org.hongxi.whatsmars.boot.cache;

import java.io.Serializable;

public record Book(String isbn, String title) implements Serializable {

    private static final long serialVersionUID = 3383972798607883427L;
}