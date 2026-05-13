package ru.practicum.stats.aggregator;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

@Slf4j
public class Aggregator {
    //  максимальный вес действий пользователя с мероприятиями
    private final Map<Long, Map<Long, Double>> eventUserActionsWeight = new HashMap<>();
    // общая сумма весов по мероприятиям
    private Map<Long, Double> totalWeightsSum = new HashMap<>();
    // частная сумма по мероприятиям
    private Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public List<EventSimilarityAvro> updateEventSimilarities(UserActionAvro userAction) {
        log.debug("Received event {}", userAction);
        Long eventId = userAction.getEventId();
        Long userId = userAction.getUserId();
        List<EventSimilarityAvro> eventSimilarityAvroList = new ArrayList<>();

        Double updatedEventUserActionWeight = getUserActionWeight(userAction);
        Boolean weightWasUpdated = false;

        // 1. Обновим максимальный вес мероприятия по данному пользователю
        if (!eventUserActionsWeight.containsKey(eventId)) {
            Map<Long, Double> weightMap = new HashMap<>();
            weightMap.put(userId, updatedEventUserActionWeight);
            weightWasUpdated = true;
            eventUserActionsWeight.put(eventId, weightMap);
        } else {
            Map<Long, Double> weightMap = eventUserActionsWeight.get(eventId);
            Double currentWeight = weightMap.get(userId);
            Double newWeight = getUserActionWeight(userAction);
            updatedEventUserActionWeight = Math.max(newWeight, weightMap.get(userId));

            weightMap.put(userId, updatedEventUserActionWeight);

            if (updatedEventUserActionWeight > currentWeight) {
                weightWasUpdated = true;
            }
        }

        if (!totalWeightsSum.containsKey(eventId)) {
            totalWeightsSum.put(userId, updatedEventUserActionWeight);
        }

        if (weightWasUpdated) {
            // Обновляем похожести со всеми мероприятиями
            for (Long id : eventUserActionsWeight.keySet()) {
                // Кроме текущего
                if (!Objects.equals(id, eventId)) {

                    // 2. Обновим общую сумму весов по мероприятию (числитель)
                    Double oldEventAWeight = eventUserActionsWeight.get(eventId).getOrDefault(userId, 0D);
                    Double oldEventBWeight = eventUserActionsWeight.get(id).getOrDefault(userId, 0D);

                    // TODO: Также помните, что сходство двух мероприятий рассчитывается на основе действий пользователя с обоими. Если пользователь не взаимодействовал с одним из них, то он не может сделать вклад в расчёт сходства этой пары мероприятий.
                    Double oldMinSum = getMinSum(eventId, id);
                    Double oldMin = Math.min(oldEventAWeight, oldEventBWeight);
                    Double newMin = Math.min(updatedEventUserActionWeight, oldEventBWeight);
                    Double deltaMin = newMin - oldMin;
                    Double newMinSum = oldMinSum + deltaMin;

                    setMinSum(eventId, id, newMinSum);

                    // 2. Обновим общую сумму весов по мероприятию (знаменатель)
                    Double deltaWeight = updatedEventUserActionWeight - oldEventAWeight;
                    Double totalAOld = totalWeightsSum.getOrDefault(eventId, 0D);
                    Double totalBOld = totalWeightsSum.getOrDefault(id, 0D);
                    Double totalANew = totalAOld + deltaWeight;
                    totalWeightsSum.put(id, totalANew);

                    Double denominator = (Math.sqrt(totalANew)) * Math.sqrt(totalBOld);
                    Double similarity = denominator != 0 ? newMinSum / denominator : 0;

                    EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro
                            .newBuilder()
                            .setEventA(eventId)
                            .setEventB(id)
                            .setScore(similarity)
                            .setTimestamp(userAction.getTimestamp())
                            .build();

                    eventSimilarityAvroList.add(eventSimilarityAvro);
                }
            }
        }

        return eventSimilarityAvroList;
    }

    private Double getUserActionWeight(UserActionAvro userAction) {
        return switch (userAction.getActionType()) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> 0.0;
        };
    }

    public void setMinSum(long eventA, long eventB, double sum) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    public double getMinSum(long eventA, long eventB) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

}
