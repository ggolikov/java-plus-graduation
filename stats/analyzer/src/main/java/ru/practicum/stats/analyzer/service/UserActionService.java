package ru.practicum.stats.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.*;
import ru.practicum.stats.analyzer.mapper.InteractionMapper;
import ru.practicum.stats.analyzer.model.Interaction;
import ru.practicum.stats.analyzer.model.Similarity;
import ru.practicum.stats.analyzer.repository.InteractionRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserActionService {
    private final InteractionRepository interactionRepository;
    private final EventSimilarityService eventSimilarityService;

    public UserActionService(InteractionRepository interactionRepository, EventSimilarityService eventSimilarityService) {
        this.interactionRepository = interactionRepository;
        this.eventSimilarityService = eventSimilarityService;
    }
    public void processEvent(UserActionAvro event) {
        Interaction interaction = InteractionMapper.mapToInteraction(event);
        interactionRepository.save(interaction);
    }
    public Long getInteractionsCount(InteractionsCountRequestProto request) {
        return interactionRepository.countRatingByEventId(request.getEventId());
    }
    public List<Interaction> getUserActions(UserPredictionsRequestProto request) {
        return interactionRepository.findUserActions(request.getUserId());
    }

    public List<Similarity> getUserPredictions(UserPredictionsRequestProto request) {
        List<Interaction> userActions = getUserActions(request);
        List<Long> userActionsIds = userActions.stream().map(Interaction::getId).toList();
        List<Similarity> similarities = eventSimilarityService.findSimilarEventsByIds(userActionsIds);

        List<Similarity> newSimilarities = similarities.stream().filter(s -> !userActionsIds.contains(s.getEvent1()) && !userActionsIds.contains(s.getEvent2()) ).toList().subList(0, ((int) request.getMaxResults()));

        return newSimilarities;
    }
}
