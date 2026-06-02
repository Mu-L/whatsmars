package org.hongxi.whatsmars.nacos;

import org.hongxi.whatsmars.nacos.properties.AgentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BeanConfigController {

    @Autowired
    private AgentProperties agentProperties;

    @GetMapping("/config/agent")
    public AgentProperties agent() {
        return agentProperties;
    }
}