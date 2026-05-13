package ru.practicum.kafka.deserializer;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventSimilaritiesDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {
    public EventSimilaritiesDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
