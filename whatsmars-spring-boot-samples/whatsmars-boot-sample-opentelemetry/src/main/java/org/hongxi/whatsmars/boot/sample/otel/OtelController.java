package org.hongxi.whatsmars.boot.sample.otel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class OtelController {

    private static final Logger log = LoggerFactory.getLogger(OtelController.class);

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/hello")
    public String hello(String name) {
        log.info("Calling boot-sample-web via RestTemplate - {}", name);
        return restTemplate.getForObject(
                "http://localhost:8080/hello?name=" + name, String.class);
    }
}
