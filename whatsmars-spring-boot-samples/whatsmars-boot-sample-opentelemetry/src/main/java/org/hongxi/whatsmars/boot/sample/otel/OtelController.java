package org.hongxi.whatsmars.boot.sample.otel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
public class OtelController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/hello")
    public String hello(String name) {
        log.info("Calling boot-sample-web via RestTemplate - {}", name);
        return restTemplate.getForObject(
                "http://localhost:8080/hello?name=" + name, String.class);
    }
}
