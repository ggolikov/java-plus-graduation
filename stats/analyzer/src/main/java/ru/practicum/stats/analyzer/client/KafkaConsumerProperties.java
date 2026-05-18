package ru.practicum.stats.analyzer.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "analyzer.kafka.consumer.properties")
public class KafkaConsumerProperties {
    private String bootstrapServers;
    private String groupId;
    private String actionsTopic;
    private String similaritiesTopic;
    private Duration requestTimeout = Duration.ofMillis(100);
}
