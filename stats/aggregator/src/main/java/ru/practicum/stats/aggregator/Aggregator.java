package ru.practicum.stats.aggregator;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.*;

@Slf4j
public class Aggregator {

    private final Set<Long> users = new HashSet<>();

    // eventId -> (userId -> maxWeight)
    private final Map<Long, Map<Long, Double>> eventUserActionsWeight = new HashMap<>();

    // eventId -> total weights sum
    private final Map<Long, Double> totalWeightsSum = new HashMap<>();

    // eventA -> (eventB -> min weights sum)
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public List<EventSimilarityAvro> updateEventSimilarities(UserActionAvro userAction) {
        log.debug("Received event {}", userAction);

        Long eventId = userAction.getEventId();
        Long userId = userAction.getUserId();

        users.add(userId);

        double oldWeight = getCurrentUserWeight(eventId, userId);
        double newWeight = getUserActionWeight(userAction);

        boolean updated = updateEventUserWeight(eventId, userId, oldWeight, newWeight);

        if (!updated) {
            return Collections.emptyList();
        }

        double deltaWeight = newWeight - oldWeight;
        double totalANew = updateTotalWeight(eventId, deltaWeight);

        return buildSimilarities(
                userAction,
                eventId,
                userId,
                oldWeight,
                newWeight,
                totalANew
        );
    }

    private double getCurrentUserWeight(Long eventId, Long userId) {
        return eventUserActionsWeight
                .getOrDefault(eventId, Collections.emptyMap())
                .getOrDefault(userId, 0D);
    }

    private boolean updateEventUserWeight(Long eventId,
                                          Long userId,
                                          double oldWeight,
                                          double newWeight) {

        eventUserActionsWeight
                .computeIfAbsent(eventId, id -> new HashMap<>());

        Map<Long, Double> weightMap = eventUserActionsWeight.get(eventId);

        if (newWeight > oldWeight) {
            weightMap.put(userId, newWeight);
            return true;
        }

        return false;
    }

    private double updateTotalWeight(Long eventId, double deltaWeight) {
        double updatedTotal = totalWeightsSum.getOrDefault(eventId, 0D) + deltaWeight;
        totalWeightsSum.put(eventId, updatedTotal);

        return updatedTotal;
    }

    private List<EventSimilarityAvro> buildSimilarities(UserActionAvro userAction,
                                                        Long eventA,
                                                        Long userId,
                                                        double oldEventAWeight,
                                                        double newEventAWeight,
                                                        double totalANew) {

        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Long eventB : eventUserActionsWeight.keySet()) {

            if (Objects.equals(eventA, eventB)) {
                continue;
            }

            Double eventBWeight = getCurrentUserWeight(eventB, userId);

            if (eventBWeight == 0.0) {
                continue;
            }

            double newMinSum = updateMinSum(
                    eventA,
                    eventB,
                    oldEventAWeight,
                    newEventAWeight,
                    eventBWeight
            );

            double similarity = calculateSimilarity(
                    eventA,
                    eventB,
                    totalANew,
                    newMinSum
            );

            similarities.add(buildSimilarityAvro(
                    userAction,
                    eventA,
                    eventB,
                    similarity
            ));
        }

        return similarities;
    }

    private double updateMinSum(Long eventA,
                                Long eventB,
                                double oldEventAWeight,
                                double newEventAWeight,
                                double eventBWeight) {

        double oldMinSum = getMinSum(eventA, eventB);

        double oldMin = Math.min(oldEventAWeight, eventBWeight);
        double newMin = Math.min(newEventAWeight, eventBWeight);

        double deltaMin = newMin - oldMin;
        double newMinSum = oldMinSum + deltaMin;

        if (deltaMin != 0.0) {
            setMinSum(eventA, eventB, newMinSum);
        }

        return newMinSum;
    }

    private double calculateSimilarity(Long eventA,
                                       Long eventB,
                                       double totalANew,
                                       double minSum) {

        double totalB = totalWeightsSum.getOrDefault(eventB, 0D);

        double denominator = Math.sqrt(totalANew) * Math.sqrt(totalB);

        return denominator != 0.0
                ? minSum / denominator
                : 0.0;
    }

    private EventSimilarityAvro buildSimilarityAvro(UserActionAvro userAction,
                                                    Long eventA,
                                                    Long eventB,
                                                    double similarity) {

        return EventSimilarityAvro.newBuilder()
                .setEventA(Math.min(eventA, eventB))
                .setEventB(Math.max(eventA, eventB))
                .setScore(similarity)
                .setTimestamp(userAction.getTimestamp())
                .build();
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
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    public double getMinSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }
}