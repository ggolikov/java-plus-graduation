package ru.practicum.stats.analyzer.client;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.deserializer.EventSimilaritiesDeserializer;
import ru.practicum.kafka.deserializer.UserActionDeserializer;

import java.util.Properties;

@Component
@EnableConfigurationProperties({KafkaConsumerProperties.class})
public class KafkaClientImplementation implements KafkaClient {
    private final KafkaConsumerProperties kafkaConsumerProperties;
    private Consumer<String, EventSimilarityAvro> eventSimilaritiesConsumer;
    private Consumer<Long, UserActionAvro> userActionsConsumer;

    public KafkaClientImplementation(KafkaConsumerProperties kafkaConsumerProperties) {
        this.kafkaConsumerProperties = kafkaConsumerProperties;
    }

    public KafkaClientImplementation() {
        this.kafkaConsumerProperties = new KafkaConsumerProperties();
    }

    @Override
    public Consumer<String, EventSimilarityAvro> getEventSimilaritiesConsumer() {
        if (eventSimilaritiesConsumer == null) {
            initEventSimilaritiesConsumer();
        }

        return eventSimilaritiesConsumer;
    }

    private void initEventSimilaritiesConsumer() {
        Properties config = new Properties();
//        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerProperties.getBootstrapServers());
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilaritiesDeserializer.class.getName());
//        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroupId());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "stats.events-similarity.v1");

        eventSimilaritiesConsumer = new KafkaConsumer<>(config);
    }

    @Override
    public Consumer<Long, UserActionAvro> getUserActionsConsumer() {
        if (userActionsConsumer == null) {
            initUserActionsConsumer();
        }

        return userActionsConsumer;
    }

    private void initUserActionsConsumer() {
        Properties config = new Properties();
//        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerProperties.getBootstrapServers());
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class.getName());
//        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroupId());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "stats.user-actions.v1");

        userActionsConsumer = new KafkaConsumer<>(config);
    }
}
