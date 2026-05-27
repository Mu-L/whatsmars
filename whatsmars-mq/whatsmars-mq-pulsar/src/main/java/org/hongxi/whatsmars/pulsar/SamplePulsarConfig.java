package org.hongxi.whatsmars.pulsar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.pulsar.core.PulsarTopic;
import org.springframework.pulsar.core.PulsarTopicBuilder;

@Configuration(proxyBeanMethods = false)
public class SamplePulsarConfig {

	private static final Log logger = LogFactory.getLog(SamplePulsarConfig.class);

	private static final String TOPIC = "pulsar-test-topic";

	@Bean
	PulsarTopic pulsarTestTopic() {
		return new PulsarTopicBuilder().name(TOPIC).numberOfPartitions(1).build();
	}

	@Bean
	ApplicationRunner sendMessagesToPulsarTopic(PulsarTemplate<SampleMessage> template) {
		return (args) -> {
			for (int i = 0; i < 10; i++) {
				template.sendAsync(TOPIC, new SampleMessage(i, "async message:" + i));
				logger.info("++++++PRODUCE ASYNC MESSAGE:(" + i + ")------");
			}

			for (int i = 10; i < 20; i++) {
				template.send(TOPIC, new SampleMessage(i, "message:" + i));
				logger.info("++++++PRODUCE MESSAGE:(" + i + ")------");
			}
		};
	}

	@PulsarListener(topics = TOPIC)
	void consumeMessagesFromPulsarTopic(SampleMessage msg) {
		logger.info("++++++CONSUME MESSAGE:(" + msg.id() + ")------");
	}

}
