package ru.practicum.stats.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.stats.analyzer.mapper.InteractionMapper;
import ru.practicum.stats.analyzer.model.EventCountProjection;
import ru.practicum.stats.analyzer.model.Interaction;
import ru.practicum.stats.analyzer.model.Similarity;
import ru.practicum.stats.analyzer.repository.InteractionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserActionService {
    private final InteractionRepository interactionRepository;
    private final EventSimilarityService eventSimilarityService;

    public UserActionService(InteractionRepository interactionRepository, EventSimilarityService eventSimilarityService) {
        this.interactionRepository = interactionRepository;
        this.eventSimilarityService = eventSimilarityService;
    }
    @Transactional
    public void processEvent(UserActionAvro event) {

        Interaction newInteraction = InteractionMapper.mapToInteraction(event);

        Optional<Interaction> existingOpt =
                interactionRepository.findByUserIdAndEventId(
                        newInteraction.getUserId(),
                        newInteraction.getEventId()
                );

        if (existingOpt.isPresent()) {

            Interaction existing = existingOpt.get();
            Double existingRating = existing.getRating();
            Double newRating = newInteraction.getRating();
            if (newRating > existingRating && newRating <= 1.0) {
                // update fields
                existing.setRating(newRating);
                existing.setTs(newInteraction.getTs());

                interactionRepository.save(existing);

                log.info("Updated interaction for user {} and event {}",
                        existing.getUserId(),
                        existing.getEventId());
            }
        } else {
            interactionRepository.save(newInteraction);

            log.info("Created interaction for user {} and event {}",
                    newInteraction.getUserId(),
                    newInteraction.getEventId());
        }
    }
    public List<EventCountProjection> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> eventIds = request.getEventIdList();
        return interactionRepository.countRatingsByEventIds(eventIds);
    }
    public List<Interaction> getUserActions(UserPredictionsRequestProto request) {
        return interactionRepository.findUserActions(request.getUserId(), request.getMaxResults());
    }

    public List<Similarity> getUserPredictions(UserPredictionsRequestProto request) {
        List<Interaction> userActions = getUserActions(request);
        List<Interaction> newEventsForUser = interactionRepository.findUserNewActions(request.getUserId());

        List<Long> newUserActionsIds = newEventsForUser.stream().map(Interaction::getEventId).collect(Collectors.toList());
        List<Similarity> similarities = eventSimilarityService.findSimilarEventsByIds(newUserActionsIds);

        return similarities;
    }
}
