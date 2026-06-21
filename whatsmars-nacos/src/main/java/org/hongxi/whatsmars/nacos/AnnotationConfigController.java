package org.hongxi.whatsmars.nacos;

import com.alibaba.cloud.nacos.annotation.NacosConfig;
import com.alibaba.cloud.nacos.annotation.NacosConfigListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class AnnotationConfigController {

    private static final Logger log = LoggerFactory.getLogger(AnnotationConfigController.class);

    @NacosConfig(dataId = "github.username", group = "DEFAULT_GROUP")
    private String name;

    @GetMapping("/config/hello")
    public String hello() {
        return "Hello, " + name;
    }

    @NacosConfigListener(dataId = "github.username", group = "DEFAULT_GROUP")
    public void updated(String name) {
        log.info("updated: {}", name);
    }
}