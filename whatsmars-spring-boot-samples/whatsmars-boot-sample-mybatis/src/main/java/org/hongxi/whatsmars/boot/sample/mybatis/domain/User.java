package org.hongxi.whatsmars.boot.sample.mybatis.domain;

import java.util.Date;
import java.util.Objects;

/**
 * Created by shenhongxi on 2017/6/26.
 */
public class User {
    private Long id;

    private String username;

    private String nickname;

    private Integer gender;

    private Integer age;

    private Date createDate;

    private Date updateDate;

    public User() {}

    private User(Long id, String username, String nickname, Integer gender, Integer age,
                 Date createDate, Date updateDate) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return Objects.equals(id, that.id)
                && Objects.equals(username, that.username)
                && Objects.equals(nickname, that.nickname)
                && Objects.equals(gender, that.gender)
                && Objects.equals(age, that.age)
                && Objects.equals(createDate, that.createDate)
                && Objects.equals(updateDate, that.updateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, nickname, gender, age, createDate, updateDate);
    }

    @Override
    public String toString() {
        return "User(id=" + id + ", username=" + username + ", nickname=" + nickname
                + ", gender=" + gender + ", age=" + age + ", createDate=" + createDate
                + ", updateDate=" + updateDate + ")";
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String nickname;
        private Integer gender;
        private Integer age;
        private Date createDate;
        private Date updateDate;

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserBuilder gender(Integer gender) {
            this.gender = gender;
            return this;
        }

        public UserBuilder age(Integer age) {
            this.age = age;
            return this;
        }

        public UserBuilder createDate(Date createDate) {
            this.createDate = createDate;
            return this;
        }

        public UserBuilder updateDate(Date updateDate) {
            this.updateDate = updateDate;
            return this;
        }

        public User build() {
            return new User(id, username, nickname, gender, age, createDate, updateDate);
        }
    }
}
