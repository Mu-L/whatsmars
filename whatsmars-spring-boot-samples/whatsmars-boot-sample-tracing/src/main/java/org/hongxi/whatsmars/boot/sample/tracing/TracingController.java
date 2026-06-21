package org.hongxi.whatsmars.boot.sample.tracing;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class TracingController {

    private static final Logger log = LoggerFactory.getLogger(TracingController.class);

    @GetMapping("/hello")
    public String hello(String name, @RequestHeader(value = "traceparent", required = false) String traceparent) {
        log.info("traceparent: {}", traceparent);
        return "Hello, " + name;
    }
}
