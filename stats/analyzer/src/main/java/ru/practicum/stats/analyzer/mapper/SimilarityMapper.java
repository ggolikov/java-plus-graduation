package ru.practicum.stats.analyzer.mapper;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.stats.analyzer.model.Similarity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class SimilarityMapper {
    public static Similarity mapToSimilarity(EventSimilarityAvro event) {
        Similarity similarity = new Similarity();

        OffsetDateTime odt = OffsetDateTime.ofInstant(
                event.getTimestamp(),
                ZoneOffset.UTC
        );

        similarity.setEvent1(event.getEventA());
        similarity.setEvent2(event.getEventB());
        similarity.setSimilarity(event.getScore());
        similarity.setTs(odt);

        return similarity;
    }
}
