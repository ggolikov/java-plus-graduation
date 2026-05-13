package ru.practicum.stats.aggregator.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aggregator.kafka.consumer.properties")
public class KafkaConsumerProperties {
    private String bootstrapServers;
    private String groupId;
    private String incomingTopic;
    private String outgoingTopic;
}
