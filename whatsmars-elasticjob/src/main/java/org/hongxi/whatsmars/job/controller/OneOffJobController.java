package org.hongxi.whatsmars.job.controller;

import org.apache.shardingsphere.elasticjob.bootstrap.type.OneOffJobBootstrap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class OneOffJobController {

    @Autowired
    @Qualifier("myOneOffJobBean")
    private ObjectProvider<OneOffJobBootstrap> myOneOffJobProvider;

    @GetMapping("/execute")
    public String executeOneOffJob() {
        OneOffJobBootstrap myOneOffJob = myOneOffJobProvider.getIfAvailable();
        Objects.requireNonNull(myOneOffJob);
        myOneOffJob.execute();
        return "{\"msg\":\"OK\"}";
    }
}

