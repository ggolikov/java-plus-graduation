package ru.practicum.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.analyzer.model.Similarity;

import java.util.List;
import java.util.Optional;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId ORDER BY s.similarity DESC LIMIT :maxResults")
    List<Similarity> getEventsBySimilarity(Long eventId, Long maxResults);

    List<Similarity> findByIdIn(List<Long> ids);

    Optional<Similarity> findByEvent1AndEvent2(Long event1, Long event2);
}
