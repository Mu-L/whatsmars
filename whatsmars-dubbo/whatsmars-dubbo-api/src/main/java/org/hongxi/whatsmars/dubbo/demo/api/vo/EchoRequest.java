package org.hongxi.whatsmars.dubbo.demo.api.vo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class EchoRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2080533871181613308L;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EchoRequest that = (EchoRequest) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(message);
    }

    @Override
    public String toString() {
        return "EchoRequest(message=" + message + ")";
    }
}
