package org.hongxi.whatsmars.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2018/12/12.
 */
@Component
public class Consumer {

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    @KafkaListener(topics = "kafkaTest")
    public void onMessage(String message) {
        log.info("received message: {}", message);
    }
}
