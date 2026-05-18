package ru.practicum.stats.analyzer.mapper;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.stats.analyzer.model.Interaction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class InteractionMapper {
    public static  Interaction mapToInteraction(UserActionAvro event) {
        Interaction interaction = new Interaction();

        OffsetDateTime odt = OffsetDateTime.ofInstant(
                event.getTimestamp(),
                ZoneOffset.UTC
        );

        interaction.setEventId(event.getEventId());
        interaction.setRating(getRating(event));
        interaction.setUserId(event.getUserId());
        interaction.setTs(odt);

        return interaction;
    }

    public static Double getRating(UserActionAvro event) {
        return switch (event.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> 0.0;
        };
    }

}
