package org.hongxi.whatsmars.boot.sample.webflux.support;

import java.util.Objects;

/**
 * Created by shenhongxi on 2021/4/29.
 */
public class SessionContext {

    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionContext that = (SessionContext) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }

    @Override
    public String toString() {
        return "SessionContext(userId=" + userId + ")";
    }
}
