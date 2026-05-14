package ru.practicum.stats.aggregator;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.*;

@Slf4j
public class Aggregator {
    // Список пользователей
    Set<Long> users = new HashSet<>();
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

        users.add(userId);
        Double oldEventAWeight = eventUserActionsWeight
                .getOrDefault(eventId, Collections.emptyMap())
                .getOrDefault(userId, 0D);
        Double updatedEventUserActionWeight = getUserActionWeight(userAction);

        // 1. Обновим максимальный вес мероприятия по данному пользователю
        if (!eventUserActionsWeight.containsKey(eventId)) {
            Map<Long, Double> weightMap = new HashMap<>();
            weightMap.put(userId, updatedEventUserActionWeight);
            eventUserActionsWeight.put(eventId, weightMap);
        } else {
            Map<Long, Double> weightMap = eventUserActionsWeight.get(eventId);

            if (!weightMap.containsKey(userId)) {
                weightMap.put(userId, 0D);
            }

            Double currentWeight = weightMap.getOrDefault(userId, 0D);
            oldEventAWeight = currentWeight;

            if (updatedEventUserActionWeight > currentWeight) {
                weightMap.put(userId, updatedEventUserActionWeight);
            }
        }

        if (updatedEventUserActionWeight <= oldEventAWeight) {
            return new ArrayList<>();
        }

        Double deltaWeight = updatedEventUserActionWeight - oldEventAWeight;
        Double totalAOld = totalWeightsSum.getOrDefault(eventId, 0D);
        Double totalANew = totalAOld;

        if (deltaWeight > 0) {
            totalANew += deltaWeight;
            totalWeightsSum.put(eventId, totalANew);
        }
            // Обновляем похожести со всеми мероприятиями
            for (Long id : eventUserActionsWeight.keySet()) {
                // Кроме текущего
                if (!Objects.equals(id, eventId)) {
                    Long eventA = eventId;
                    Long eventB = id;

                    // 2. Обновим общую сумму весов по мероприятию (числитель)
                    Double oldEventBWeight = eventUserActionsWeight.get(eventB).getOrDefault(userId, 0D);

                    //  Пользователь не взаимодействовал с мероприятием B, поэтому не вносил свой вес в коэффициент сходства этих мероприятий. Пересчитывать его нет смысла.
                    if (oldEventBWeight != 0.0) {
                        // Предыдущие частные суммы для этих мероприятий были следующими:
                        Double oldMinSum = getMinSum(eventA, eventB);

                        Double totalBOld = totalWeightsSum.getOrDefault(eventB, 0D);
                        // Обновим числитель, то есть сумму минимальных весов.
                        // Сравним старый вклад пользователя в общую сумму и новый:
                        Double oldMin = Math.min(oldEventAWeight, oldEventBWeight);
                        Double newMin = Math.min(updatedEventUserActionWeight, oldEventBWeight);
                        Double deltaMin = newMin - oldMin;
                        Double newMinSum = oldMinSum + deltaMin;

                        if (deltaMin != 0.0) {
                            setMinSum(eventA, eventB, newMinSum);
                        }

                        // Обновим суммы в знаменателе. В знаменателе нужно пересчитать только сумму весов события A.
                        Double denominator = Math.sqrt(totalANew) * Math.sqrt(totalBOld);
                        Double similarity = denominator != 0.0 ? newMinSum / denominator : 0.0;

                        EventSimilarityAvro eventSimilarityAvro = EventSimilarityAvro
                                .newBuilder()
                                .setEventA(Math.min(eventA, eventB))
                                .setEventB(Math.max(eventB, eventA))
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

    public Double getEventPairMinSum(Long event1, Long event2) {
        Long eventA = Math.min(event1, event2);
        Long eventB = Math.max(event1, event2);

        Double minSum = 0.0;

        for (Long user: users) {
            Double weightA = eventUserActionsWeight.get(eventA).getOrDefault(user, 0D);
            Double weightB = eventUserActionsWeight.get(eventB).getOrDefault(user, 0D);

            minSum += Math.min(weightA, weightB);
        }

        return minSum;
    }

}
