package ru.practicum.stats.aggregator.client;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.deserializer.UserActionDeserializer;
import ru.practicum.kafka.serializer.GeneralAvroSerializer;

import java.util.Properties;

@Component
@EnableConfigurationProperties({KafkaProducerProperties.class, KafkaConsumerProperties.class})
public class KafkaClientImplementation implements KafkaClient, AutoCloseable {
    private final KafkaProducerProperties kafkaProducerProperties;
    private final KafkaConsumerProperties kafkaConsumerProperties;
    private Producer<String, EventSimilarityAvro> producer;
    private Consumer<String, UserActionAvro> consumer;

    public KafkaClientImplementation(KafkaProducerProperties kafkaProducerProperties, KafkaConsumerProperties kafkaConsumerProperties) {
        this.kafkaProducerProperties = kafkaProducerProperties;
        this.kafkaConsumerProperties = kafkaConsumerProperties;
    }

    public KafkaClientImplementation() {
        this.kafkaProducerProperties = new KafkaProducerProperties();
        this.kafkaConsumerProperties = new KafkaConsumerProperties();
    }

    @Override
    public Producer<String, EventSimilarityAvro> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    private void initProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,  "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);

        producer = new KafkaProducer<>(config);
    }

    @Override
    public Consumer<String, UserActionAvro> getConsumer() {
        if (consumer == null) {
            initConsumer();
        }

        return consumer;
    }

    private void initConsumer() {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class.getName());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "telemetry.sensors.v1");

        consumer = new KafkaConsumer<>(config);
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
        }
    }
}
