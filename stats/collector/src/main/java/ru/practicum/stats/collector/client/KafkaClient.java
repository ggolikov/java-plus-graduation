package ru.practicum.stats.collector.client;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;

public interface KafkaClient {
    Producer<Long, SpecificRecordBase> getProducer();
}

