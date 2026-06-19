package org.hongxi.whatsmars.sentinel.aop.controller;

import org.hongxi.whatsmars.sentinel.aop.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @Autowired
    private TestService service;

    @GetMapping("/hi")
    public String hi(String name) {
        return service.hi(name);
    }

    @GetMapping("/foo")
    public String foo(@RequestParam(required = false) Long t) {
        if (t == null) {
            t = System.currentTimeMillis();
        }
        return service.hello(t);
    }

    @GetMapping("/baz/{name}")
    public String baz(@PathVariable("name") String name) {
        return service.helloAnother(name);
    }
}
