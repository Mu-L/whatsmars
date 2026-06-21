package org.hongxi.whatsmars.spring.bean;

import java.util.List;
import java.util.Objects;

/**
 * Created by shenhongxi on 2021/4/2.
 */
public class Student {

    private String name;
    private Integer age;
    private Contacts address;
    private List<Contacts> addresses;

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

    public Contacts getAddress() {
        return address;
    }

    public void setAddress(Contacts address) {
        this.address = address;
    }

    public List<Contacts> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Contacts> addresses) {
        this.addresses = addresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student that = (Student) o;
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
        return "Student(name=" + name + ", age=" + age + ", address=" + address
                + ", addresses=" + addresses + ")";
    }
}
