package org.hongxi.whatsmars.boot.sample.tracing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TracingController {

    @GetMapping("/hello")
    public String hello(String name, @RequestHeader(value = "traceparent", required = false) String traceparent) {
        log.info("traceparent: {}", traceparent);
        return "Hello, " + name;
    }
}
