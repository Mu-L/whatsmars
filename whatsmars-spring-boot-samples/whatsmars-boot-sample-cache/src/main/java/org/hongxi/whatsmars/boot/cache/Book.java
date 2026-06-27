package org.hongxi.whatsmars.boot.cache;

import java.io.Serial;
import java.io.Serializable;

public record Book(String isbn, String title) implements Serializable {

    @Serial
    private static final long serialVersionUID = 3383972798607883427L;
}