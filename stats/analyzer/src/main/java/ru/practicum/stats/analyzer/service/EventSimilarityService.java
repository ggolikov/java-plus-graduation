package ru.practicum.stats.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.stats.analyzer.mapper.SimilarityMapper;
import ru.practicum.stats.analyzer.model.Similarity;
import ru.practicum.stats.analyzer.repository.SimilarityRepository;

import java.util.List;

@Slf4j
@Service
public class EventSimilarityService {
    private final SimilarityRepository similarityRepository;

    public EventSimilarityService(SimilarityRepository similarityRepository) {
        this.similarityRepository = similarityRepository;
    }
    @Transactional(readOnly = true)
    public void processEvent(EventSimilarityAvro event) {
        Similarity similarity = SimilarityMapper.mapToSimilarity(event);
        similarityRepository.save(similarity);
    }

    public List<Similarity> getSimilarEvents(SimilarEventsRequestProto request) {
        return similarityRepository.getEventsBySimilarity(request.getEventId(), request.getMaxResults());
    }

    public List<Similarity> findSimilarEventsByIds(List<Long> ids) {
        return similarityRepository.findByIdIn(ids);
    }
}
