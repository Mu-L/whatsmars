package org.hongxi.whatsmars.kafka.multi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Kafka 多集群应用启动类
 * <p>
 * 演示向多个 Kafka 集群发送消息的场景
 */
@SpringBootApplication
public class KafkaApplication implements CommandLineRunner {

    @Autowired
    @Qualifier("kafkaTemplate")
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    @Qualifier("kafkaTemplate2")
    private KafkaTemplate<String, String> kafkaTemplate2;

    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class, args);
    }

    /**
     * 应用启动后执行的消息发送逻辑
     * <p>
     * 分别向两个不同的 Kafka 集群发送测试消息
     *
     * @param strings 命令行参数
     * @throws Exception 执行异常
     */
    @Override
    public void run(String... strings) throws Exception {
        kafkaTemplate.send("kafkaTest", "hello");
        kafkaTemplate2.send("kafkaTest2", "hello");
    }
}
