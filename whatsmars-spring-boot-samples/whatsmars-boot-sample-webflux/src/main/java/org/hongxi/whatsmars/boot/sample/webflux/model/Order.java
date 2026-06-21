package org.hongxi.whatsmars.boot.sample.webflux.model;

import java.util.Objects;

public class Order {

    private String id;

    private Long start;

    public Order() {
    }

    public Order(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order that = (Order) o;
        return Objects.equals(id, that.id) && Objects.equals(start, that.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, start);
    }

    @Override
    public String toString() {
        return "Order(id=" + id + ", start=" + start + ")";
    }
}
