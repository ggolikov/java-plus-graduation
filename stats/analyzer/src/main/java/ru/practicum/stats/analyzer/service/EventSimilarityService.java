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
import java.util.Optional;

@Slf4j
@Service
public class EventSimilarityService {
    private final SimilarityRepository similarityRepository;

    public EventSimilarityService(SimilarityRepository similarityRepository) {
        this.similarityRepository = similarityRepository;
    }
    @Transactional
    public void processEvent(EventSimilarityAvro event) {

        Similarity newSimilarity = SimilarityMapper.mapToSimilarity(event);

        Optional<Similarity> existingOpt =
                similarityRepository.findByEvent1AndEvent2(
                        newSimilarity.getEvent1(),
                        newSimilarity.getEvent2()
                );

        if (existingOpt.isPresent()) {
            Similarity existing = existingOpt.get();

            existing.setSimilarity(newSimilarity.getSimilarity());
            existing.setTs(newSimilarity.getTs());

            similarityRepository.save(existing);

            log.info("Updated similarity for events {} and {}",
                    existing.getEvent1(),
                    existing.getEvent2());

        } else {
            similarityRepository.save(newSimilarity);

            log.info("Created similarity for events {} and {}",
                    newSimilarity.getEvent1(),
                    newSimilarity.getEvent2());
        }
    }

    public List<Similarity> getSimilarEvents(SimilarEventsRequestProto request) {
        return similarityRepository.getEventsBySimilarity(request.getEventId(), request.getMaxResults());
    }

    public List<Similarity> findSimilarEventsByIds(List<Long> ids) {
        return similarityRepository.findByIdIn(ids);
    }

    public List<Similarity> getSimilarEvents(Long eventId, List<Long> eventIds, Long maxResults) {
        return  similarityRepository.getSimilarEvents(eventId, eventIds, maxResults);
    }
}
