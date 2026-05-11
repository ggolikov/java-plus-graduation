package ru.practicum.stats.collector.client;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaClientConfiguration {
    public static KafkaClient initClient() {
        return new KafkaClientImplementation(new KafkaProperties());
    }

    private final KafkaClient kafkaClient;

    public KafkaClientConfiguration() {
        if (this.getClient() == null) {
            this.kafkaClient = initClient();
        } else {
            this.kafkaClient = this.getClient();
        }
    }

    KafkaClient getClient() {
        return kafkaClient;
    }
}
