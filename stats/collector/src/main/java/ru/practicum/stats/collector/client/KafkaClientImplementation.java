package ru.practicum.stats.collector.client;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.serializer.GeneralAvroSerializer;
import java.util.Properties;

@Component
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaClientImplementation implements KafkaClient, AutoCloseable {
    private final KafkaProperties kafkaProperties;
    private Producer<Long, SpecificRecordBase> producer;

    public KafkaClientImplementation(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Override
    public Producer<Long, SpecificRecordBase> getProducer() {
        if (producer == null) {
            initProducer();
        }
        return producer;
    }

    private void initProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
        config.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getAcks());
        config.put(ProducerConfig.RETRIES_CONFIG, kafkaProperties.getRetries());

        producer = new KafkaProducer<>(config);
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
        }
    }
}
