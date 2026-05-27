package org.hongxi.whatsmars.nacos;

import com.alibaba.cloud.nacos.annotation.NacosConfig;
import com.alibaba.cloud.nacos.annotation.NacosConfigListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/config/test")
public class ConfigTestController {

    @NacosConfig(dataId = "github.username", group = "DEFAULT_GROUP")
    private String name;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, " + name;
    }

    @NacosConfigListener(dataId = "github.username", group = "DEFAULT_GROUP")
    public void updated(String name) {
        log.info("updated: {}", name);
    }
}
