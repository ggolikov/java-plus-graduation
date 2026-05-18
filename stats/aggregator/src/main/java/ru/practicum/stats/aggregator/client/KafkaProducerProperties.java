package ru.practicum.stats.aggregator.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aggregator.kafka.producer.properties")
public class KafkaProducerProperties {
    private String bootstrapServers;
    private String acks;
    private String retries;
}
