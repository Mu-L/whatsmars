package org.hongxi.whatsmars.boot.sample.async;

import org.hongxi.whatsmars.boot.sample.async.circular.BeanA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2018/5/8.
 */
@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private BeanA a;

    @Async("taskExecutor")
    public CompletableFuture<String> hello(String message) {
        return CompletableFuture.completedFuture("Hello, " + message);
    }

    @Async("taskExecutor")
    public void send(String message) {
        log.info("send {}", message);
    }

    public void send() {
        a.send();
    }
}
