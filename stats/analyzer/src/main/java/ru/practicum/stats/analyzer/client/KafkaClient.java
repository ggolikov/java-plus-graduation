package ru.practicum.stats.analyzer.client;

import org.apache.kafka.clients.consumer.Consumer;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface KafkaClient {
    Consumer<String, EventSimilarityAvro> getEventSimilaritiesConsumer();
    Consumer<Long, UserActionAvro> getUserActionsConsumer();
}

