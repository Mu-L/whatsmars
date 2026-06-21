package org.hongxi.whatsmars.spring.bean;

import java.util.List;
import java.util.Objects;

/**
 * Created by shenhongxi on 2021/4/2.
 */
public class User {

    private String name;
    private Integer age;
    private Address address;
    private List<Address> addresses;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return Objects.equals(name, that.name)
                && Objects.equals(age, that.age)
                && Objects.equals(address, that.address)
                && Objects.equals(addresses, that.addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, address, addresses);
    }

    @Override
    public String toString() {
        return "User(name=" + name + ", age=" + age + ", address=" + address
                + ", addresses=" + addresses + ")";
    }
}
