package ru.practicum.stats.aggregator.client;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface KafkaClient {
    Producer<String, EventSimilarityAvro> getProducer();
    Consumer<String, UserActionAvro> getConsumer();
}

