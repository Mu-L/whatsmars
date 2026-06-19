package org.hongxi.whatsmars.sentinel.webmvc.controller;

import org.hongxi.whatsmars.common.result.Result;
import org.hongxi.whatsmars.common.result.ResultHelper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebMvcTestController {

    @GetMapping("/hello")
    public Result<String> hello(String name) {
        return ResultHelper.newSuccessResult("Hello, " + name);
    }

    @GetMapping("/user/{id}")
    public Result<String> user(@PathVariable("id") Long id) {
        return ResultHelper.newSuccessResult("user-" + id);
    }
}
