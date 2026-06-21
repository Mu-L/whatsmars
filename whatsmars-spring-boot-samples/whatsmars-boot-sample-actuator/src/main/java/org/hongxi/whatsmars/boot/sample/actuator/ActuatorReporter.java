package org.hongxi.whatsmars.boot.sample.actuator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2020/7/30.
 */
public class ActuatorReporter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ActuatorReporter.class);

    @Autowired(required = false)
    private MetricsEndpoint metricsEndpoint;

    private ExecutorService executorService;

    @Override
    public void run(String... args) throws Exception {
        log.info("metrics endpoint is open : {}", metricsEndpoint != null);
        if (metricsEndpoint != null) {
            Set<String> names = metricsEndpoint.listNames().getNames();
            if (names.isEmpty()) {
                return;
            }

            executorService = Executors.newFixedThreadPool(4);
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                    () -> report(names), 1, 3, TimeUnit.SECONDS
            );
        }
    }

    private void report(Set<String> names) {
        names.forEach(name -> {
            executorService.submit(() -> {
                // store in time series database
            });
        });
    }
}
