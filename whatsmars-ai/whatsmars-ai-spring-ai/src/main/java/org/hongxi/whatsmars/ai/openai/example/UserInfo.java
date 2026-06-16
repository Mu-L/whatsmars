package org.hongxi.whatsmars.ai.openai.example;

import java.util.List;

/**
 * 用户信息 DTO - 用于结构化输出
 *
 * @author hongxi
 */
public class UserInfo {

    private String name;
    private int age;
    private String email;
    private List<String> hobbies;
    private String occupation;

    public UserInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", hobbies=" + hobbies +
                ", occupation='" + occupation + '\'' +
                '}';
    }
}
