package ru.practicum.stats.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.analyzer.model.Similarity;

import java.util.List;
import java.util.Optional;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {
    @Query("SELECT s FROM Similarity s WHERE s.event1 = :eventId ORDER BY s.similarity DESC LIMIT :maxResults")
    List<Similarity> getEventsBySimilarity(Long eventId, Long maxResults);

    List<Similarity> findByIdIn(List<Long> ids);

    Optional<Similarity> findByEvent1AndEvent2(Long event1, Long event2);

    @Query(value = """
        SELECT * FROM similarities s
                WHERE s.event1 = :eventId AND s.event2 in :eventIds
                        SORT BY similarity DESC
                LIMIT :maxResults;
            """, nativeQuery = true)
    List<Similarity> getSimilarEvents(@Param("eventId") Long eventId, @Param("eventIds") List<Long> eventIds, @Param("maxResults") Long maxResults);
}
