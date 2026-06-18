package org.hongxi.whatsmars.boot.sample.virtual.thread;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VirtualThreadController {

    /**
     * 使用 Apache Bench 进行压测
     *     ab -n 1000 -c 1000 -k http://localhost:8080/virtual-thread
     * @return
     */
    @GetMapping("/virtual-thread")
    public String hello() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Hello, World!";
    }
}
