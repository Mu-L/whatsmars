package org.hongxi.whatsmars.boot.sample.mybatis;

import org.hongxi.whatsmars.boot.sample.mybatis.dao.trade.OrderMapper;
import org.hongxi.whatsmars.boot.sample.mybatis.dao.user.UserMapper;
import org.hongxi.whatsmars.boot.sample.mybatis.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2018/12/9.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        userMapper.createIfNotExistsTable();
        orderMapper.createIfNotExistsTable();
        User user = User.builder().username("javahongxi").nickname("hongxi").gender(1).age(29).build();
        User u = userMapper.findByUsername(user.getUsername());
        if (u != null) {
            log.info("user.user exists, {}", u);
        } else {
            userMapper.insert(user);
        }
        u = orderMapper.findByUsername(user.getUsername());
        if (u != null) {
            log.info("trade.user exists, {}", u);
        } else {
            orderMapper.insert(user);
        }
    }
}
