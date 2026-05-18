package ru.practicum.stats.collector.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "collector.kafka.producer.properties")
public class KafkaProperties {
    private String bootstrapServers;
    private String acks;
    private String retries;
}