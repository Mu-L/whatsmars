package org.hongxi.whatsmars.spring.bean;

import java.util.Objects;

/**
 * Created by shenhongxi on 2021/4/2.
 */
public class Contacts {

    private String email;
    private String home;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contacts that = (Contacts) o;
        return Objects.equals(email, that.email) && Objects.equals(home, that.home);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, home);
    }

    @Override
    public String toString() {
        return "Contacts(email=" + email + ", home=" + home + ")";
    }
}
